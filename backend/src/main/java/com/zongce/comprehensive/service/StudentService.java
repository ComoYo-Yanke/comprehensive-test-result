package com.zongce.comprehensive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.common.PageResult;
import com.zongce.comprehensive.common.PasswordUtil;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.ActivityCreateDTO;
import com.zongce.comprehensive.dto.ExtraItemAddDTO;
import com.zongce.comprehensive.dto.SelfUpdateDTO;
import com.zongce.comprehensive.entity.Clazz;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.ComAssessExtraItem;
import com.zongce.comprehensive.entity.ComAssessScore;
import com.zongce.comprehensive.entity.Employee;
import com.zongce.comprehensive.entity.Major;
import com.zongce.comprehensive.entity.MessageNotification;
import com.zongce.comprehensive.entity.PenaltyRecord;
import com.zongce.comprehensive.entity.School;
import com.zongce.comprehensive.entity.Student;
import com.zongce.comprehensive.entity.StudentMajor;
import com.zongce.comprehensive.mapper.ClazzMapper;
import com.zongce.comprehensive.mapper.ComAssessActivityMapper;
import com.zongce.comprehensive.mapper.ComAssessExtraItemMapper;
import com.zongce.comprehensive.mapper.ComAssessScoreMapper;
import com.zongce.comprehensive.mapper.EmployeeMapper;
import com.zongce.comprehensive.mapper.MajorMapper;
import com.zongce.comprehensive.mapper.MessageNotificationMapper;
import com.zongce.comprehensive.mapper.PenaltyRecordMapper;
import com.zongce.comprehensive.mapper.SchoolMapper;
import com.zongce.comprehensive.mapper.StudentMajorMapper;
import com.zongce.comprehensive.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生端业务服务
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;
    private final ClazzMapper clazzMapper;
    private final SchoolMapper schoolMapper;
    private final MajorMapper majorMapper;
    private final StudentMajorMapper studentMajorMapper;
    private final EmployeeMapper employeeMapper;
    private final ComAssessActivityMapper activityMapper;
    private final ComAssessExtraItemMapper extraItemMapper;
    private final ComAssessScoreMapper scoreMapper;
    private final PenaltyRecordMapper penaltyMapper;
    private final MessageNotificationMapper notificationMapper;
    private final ActivityService activityService;
    private final NotifyService notifyService;

    // ============ 个人信息 ============

    /** 查询个人信息（含班级/学院/专业名称） */
    public Student getProfile(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        enrichStudent(student);
        student.setPassword(null);
        return student;
    }

    /** 修改个人信息：仅电话、邮箱、密码、描述 */
    @Transactional(rollbackFor = Exception.class)
    public void updateSelf(Long studentId, SelfUpdateDTO dto) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        if (dto.getPhone() != null) {
            student.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            student.setEmail(dto.getEmail());
        }
        if (dto.getDescription() != null) {
            student.setDescription(dto.getDescription());
        }
        // 修改密码需校验原密码
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            if (dto.getOldPassword() == null || !PasswordUtil.matches(dto.getOldPassword(), student.getPassword())) {
                throw new BusinessException("原密码错误");
            }
            student.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        }
        studentMapper.updateById(student);
    }

    // ============ 活动 ============

    /** 学生端活动列表（多条件筛选 + 分页） */
    public PageResult<ComAssessActivity> pageActivities(long page, long size, Integer type, Long schoolId,
                                                        String name, Integer status, Integer full,
                                                        Integer joined, Integer minNum, Integer maxNum,
                                                        Long studentId) {
        LambdaQueryWrapper<ComAssessActivity> wrapper = new LambdaQueryWrapper<>();
        // 学生只能看到审核通过、举办中、已结束的活动
        if (status != null) {
            wrapper.eq(ComAssessActivity::getStatus, status);
        } else {
            wrapper.in(ComAssessActivity::getStatus,
                    Constants.ACTIVITY_STATUS_APPROVED, Constants.ACTIVITY_STATUS_FINISHED);
        }
        if (type != null) {
            wrapper.eq(ComAssessActivity::getType, type);
        }
        if (schoolId != null) {
            wrapper.eq(ComAssessActivity::getSchoolId, schoolId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(ComAssessActivity::getName, name);
        }
        // 已满 / 未满
        if (full != null) {
            String sub = "(SELECT COUNT(*) FROM activity_student s WHERE s.activity_id = com_assess_activity.id)";
            if (full == 1) {
                wrapper.apply(sub + " >= com_assess_activity.limit_num");
            } else {
                wrapper.apply(sub + " < com_assess_activity.limit_num");
            }
        }
        // 人数范围
        if (minNum != null) {
            wrapper.apply("(SELECT COUNT(*) FROM activity_student s WHERE s.activity_id = com_assess_activity.id) >= " + minNum);
        }
        if (maxNum != null) {
            wrapper.apply("(SELECT COUNT(*) FROM activity_student s WHERE s.activity_id = com_assess_activity.id) <= " + maxNum);
        }
        // 已参加 / 未参加
        if (joined != null && studentId != null) {
            String sub = "SELECT activity_id FROM activity_student WHERE student_id = " + studentId;
            if (joined == 1) {
                wrapper.inSql(ComAssessActivity::getId, sub);
            } else if (joined == 2) {
                wrapper.notInSql(ComAssessActivity::getId, sub);
            }
        }
        wrapper.orderByDesc(ComAssessActivity::getCreateTime);

        Page<ComAssessActivity> result = activityMapper.selectPage(new Page<>(page, size), wrapper);
        activityService.enrichList(result.getRecords(), studentId);
        return PageResult.of(result);
    }

    /** 活动详情 */
    public ComAssessActivity getActivityDetail(Long activityId, Long studentId) {
        ComAssessActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activityService.enrich(activity, studentId);
        return activity;
    }

    /** 报名活动 */
    public void joinActivity(Long activityId, Long studentId) {
        activityService.join(activityId, studentId);
    }

    /** 学生会/社团成员申请创建活动 */
    public ComAssessActivity createActivity(ActivityCreateDTO dto, Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        // 只有学生会成员或社团成员可申请创建活动
        if (student.getRole() != Constants.STUDENT_ROLE_STUDENT_UNION
                && student.getRole() != Constants.STUDENT_ROLE_CLUB) {
            throw new BusinessException("仅学生会或社团成员可申请创建活动");
        }
        // 关联数据校验：创办学院、负责老师、负责学生必须真实存在
        if (dto.getSchoolId() != null && schoolMapper.selectById(dto.getSchoolId()) == null) {
            throw new BusinessException("所选学院不存在或已删除");
        }
        if (dto.getEmpInChargeIds() != null) {
            for (Long empId : dto.getEmpInChargeIds()) {
                if (employeeMapper.selectById(empId) == null) {
                    throw new BusinessException("所选负责老师不存在或已删除");
                }
            }
        }
        if (dto.getStudentInChargeIds() != null) {
            for (Long sid : dto.getStudentInChargeIds()) {
                if (studentMapper.selectById(sid) == null) {
                    throw new BusinessException("所选负责学生不存在或已删除");
                }
            }
        }
        return activityService.create(dto, studentId);
    }

    // ============ 其他综测加分 ============

    /** 添加其他综测加分项，并通知本班辅导员 */
    @Transactional(rollbackFor = Exception.class)
    public void addExtraItem(Long studentId, ExtraItemAddDTO dto) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        ComAssessExtraItem item = new ComAssessExtraItem();
        item.setName(dto.getName());
        item.setStudentId(studentId);
        item.setEvidence(dto.getEvidence());
        item.setScore(dto.getScore());
        item.setDescription(dto.getDescription());
        item.setStatus(Constants.AUDIT_STATUS_UNAUDITED);
        extraItemMapper.insert(item);

        // 通知本班辅导员
        Long counselorId = findCounselorByClazz(student.getClazzId());
        if (counselorId != null) {
            notifyService.send(counselorId, Constants.RECEIVER_EMPLOYEE,
                    "新的综测加分申请", "学生「" + student.getName() + "」提交了加分项「" + dto.getName() + "」，请及时审核。",
                    2, item.getId());
        }
    }

    /** 查询自己的加分项（分页，支持按状态/名称筛选） */
    public PageResult<ComAssessExtraItem> pageMyExtraItems(long page, long size, Long studentId, Integer status, String name) {
        LambdaQueryWrapper<ComAssessExtraItem> wrapper = new LambdaQueryWrapper<ComAssessExtraItem>()
                .eq(ComAssessExtraItem::getStudentId, studentId);
        if (status != null) {
            wrapper.eq(ComAssessExtraItem::getStatus, status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(ComAssessExtraItem::getName, name);
        }
        wrapper.orderByDesc(ComAssessExtraItem::getCreateTime);
        Page<ComAssessExtraItem> result = extraItemMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    // ============ 综测成绩 ============

    /** 计算综测成绩 */
    @Transactional(rollbackFor = Exception.class)
    public ComAssessScore computeScore(Long studentId, Integer year, Integer semester) {
        // 存在未审核结果则不能再次提交
        Long unapproved = scoreMapper.selectCount(new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStudentId, studentId)
                .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_UNAUDITED));
        if (unapproved != null && unapproved > 0) {
            throw new BusinessException("存在未审核的综测结果，请先等待审核");
        }

        // 计算年份与学期（默认当前）
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (semester == null) {
            semester = currentSemester();
        }

        // 一年最多 2 个审核通过
        Long approvedCount = scoreMapper.selectCount(new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStudentId, studentId)
                .eq(ComAssessScore::getYear, year)
                .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_APPROVED));
        if (approvedCount != null && approvedCount >= Constants.SCORE_MAX_APPROVED_PER_YEAR) {
            throw new BusinessException("本年已通过 " + Constants.SCORE_MAX_APPROVED_PER_YEAR + " 个综测，无法继续提交");
        }

        // 活动分：已参加且审核通过的活动按类型计分
        BigDecimal activityScore = activityService.listJoinedApproved(studentId).stream()
                .map(activityService::scoreOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 其他加分：审核通过的加分项之和
        List<ComAssessExtraItem> approvedItems = extraItemMapper.selectList(new LambdaQueryWrapper<ComAssessExtraItem>()
                .eq(ComAssessExtraItem::getStudentId, studentId)
                .eq(ComAssessExtraItem::getStatus, Constants.AUDIT_STATUS_APPROVED));
        BigDecimal extraScore = approvedItems.stream()
                .map(ComAssessExtraItem::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 违规扣分
        List<PenaltyRecord> penalties = penaltyMapper.selectList(new LambdaQueryWrapper<PenaltyRecord>()
                .eq(PenaltyRecord::getStudentId, studentId));
        BigDecimal penaltyScore = penalties.stream()
                .map(PenaltyRecord::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = activityScore.add(extraScore).subtract(penaltyScore);

        ComAssessScore score = new ComAssessScore();
        score.setStudentId(studentId);
        score.setYear(year);
        score.setSemester(semester);
        score.setActivityScore(activityScore);
        score.setExtraScore(extraScore);
        score.setPenaltyScore(penaltyScore);
        score.setScore(total);
        score.setStatus(Constants.AUDIT_STATUS_UNAUDITED);
        scoreMapper.insert(score);
        return score;
    }

    /** 查询自己的综测成绩（分页，支持按状态/学年/学期筛选） */
    public PageResult<ComAssessScore> pageMyScores(long page, long size, Long studentId, Integer status,
                                                   Integer year, Integer semester) {
        LambdaQueryWrapper<ComAssessScore> wrapper = new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStudentId, studentId);
        if (status != null) {
            wrapper.eq(ComAssessScore::getStatus, status);
        }
        if (year != null) {
            wrapper.eq(ComAssessScore::getYear, year);
        }
        if (semester != null) {
            wrapper.eq(ComAssessScore::getSemester, semester);
        }
        wrapper.orderByDesc(ComAssessScore::getCreateTime);
        Page<ComAssessScore> result = scoreMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    /** 删除审核不通过的综测成绩 */
    public void deleteScore(Long scoreId, Long studentId) {
        ComAssessScore score = scoreMapper.selectById(scoreId);
        if (score == null || !score.getStudentId().equals(studentId)) {
            throw new BusinessException("综测记录不存在");
        }
        if (score.getStatus() != Constants.AUDIT_STATUS_REJECTED) {
            throw new BusinessException("只有审核不通过的综测才能删除");
        }
        scoreMapper.deleteById(scoreId);
    }

    // ============ 违规记录 / 通知 ============

    /** 查询自己的违规记录（分页，原因模糊、处分精确） */
    public PageResult<PenaltyRecord> pageMyPenalties(long page, long size, Long studentId,
                                                     String reason, String punishment) {
        LambdaQueryWrapper<PenaltyRecord> wrapper = new LambdaQueryWrapper<PenaltyRecord>()
                .eq(PenaltyRecord::getStudentId, studentId);
        if (reason != null && !reason.isEmpty()) {
            wrapper.like(PenaltyRecord::getReason, reason);
        }
        if (punishment != null && !punishment.isEmpty()) {
            wrapper.eq(PenaltyRecord::getPunishment, punishment);
        }
        wrapper.orderByDesc(PenaltyRecord::getCreateTime);
        return PageResult.of(penaltyMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /** 查询自己的通知（分页） */
    public PageResult<MessageNotification> pageMyNotifications(long page, long size, Long studentId) {
        Page<MessageNotification> result = notificationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MessageNotification>()
                        .eq(MessageNotification::getReceiverId, studentId)
                        .eq(MessageNotification::getReceiverType, Constants.RECEIVER_STUDENT)
                        .orderByDesc(MessageNotification::getCreateTime));
        return PageResult.of(result);
    }

    /** 标记通知已读 */
    public void markNotificationRead(Long id, Long studentId) {
        MessageNotification notification = notificationMapper.selectById(id);
        if (notification == null || !notification.getReceiverId().equals(studentId)) {
            throw new BusinessException("通知不存在");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    // ============ 内部工具 ============

    /** 填充学生展示字段（班级名、学院名、专业名） */
    public void enrichStudent(Student student) {
        if (student.getClazzId() != null) {
            Clazz clazz = clazzMapper.selectById(student.getClazzId());
            student.setClazzName(clazz == null ? null : clazz.getName());
        }
        if (student.getSchoolId() != null) {
            School school = schoolMapper.selectById(student.getSchoolId());
            student.setSchoolName(school == null ? null : school.getName());
        }
        List<Long> majorIds = studentMajorMapper.selectList(new LambdaQueryWrapper<StudentMajor>()
                        .eq(StudentMajor::getStudentId, student.getId()))
                .stream().map(StudentMajor::getMajorId).toList();
        student.setMajorIds(majorIds);
        if (!majorIds.isEmpty()) {
            String names = majorMapper.selectBatchIds(majorIds).stream()
                    .map(Major::getName).collect(Collectors.joining("、"));
            student.setMajorNames(names);
        }
    }

    /** 学生申请创建活动弹窗的下拉数据：正常学院、可选负责老师（仅 id/name） */
    public Map<String, Object> activityOptions() {
        List<Map<String, Object>> schools = schoolMapper.selectList(new LambdaQueryWrapper<School>()
                        .eq(School::getStatus, Constants.SCHOOL_STATUS_NORMAL)
                        .orderByAsc(School::getId))
                .stream().map(s -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", s.getId());
                    item.put("name", s.getName());
                    return item;
                }).toList();
        List<Map<String, Object>> teachers = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                        .in(Employee::getRole, Constants.EMPLOYEE_ROLE_TEACHER,
                                Constants.EMPLOYEE_ROLE_LEADER, Constants.EMPLOYEE_ROLE_COUNSELOR)
                        .eq(Employee::getStatus, Constants.EMPLOYEE_STATUS_ON)
                        .orderByAsc(Employee::getId))
                .stream().map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", e.getId());
                    item.put("name", e.getName());
                    return item;
                }).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schools", schools);
        result.put("teachers", teachers);
        return result;
    }

    /** 根据班级找辅导员 */
    public Long findCounselorByClazz(Long clazzId) {
        if (clazzId == null) {
            return null;
        }
        Employee counselor = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getClazzId, clazzId)
                .eq(Employee::getRole, Constants.EMPLOYEE_ROLE_COUNSELOR)
                .last("LIMIT 1"));
        return counselor == null ? null : counselor.getId();
    }

    /** 当前学期：3-8 月为春季(1)，其余为秋季(2) */
    private int currentSemester() {
        int month = LocalDate.now().getMonthValue();
        return (month >= 3 && month <= 8) ? 1 : 2;
    }
}
