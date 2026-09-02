package com.zongce.comprehensive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.common.PageResult;
import com.zongce.comprehensive.common.PasswordUtil;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.PenaltyAddDTO;
import com.zongce.comprehensive.dto.ReviewDTO;
import com.zongce.comprehensive.entity.Clazz;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.ComAssessExtraItem;
import com.zongce.comprehensive.entity.ComAssessScore;
import com.zongce.comprehensive.entity.Employee;
import com.zongce.comprehensive.entity.EmployeeSchool;
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
import com.zongce.comprehensive.mapper.EmployeeSchoolMapper;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 员工端（管理端）业务服务
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final StudentMapper studentMapper;
    private final StudentMajorMapper studentMajorMapper;
    private final EmployeeMapper employeeMapper;
    private final EmployeeSchoolMapper employeeSchoolMapper;
    private final SchoolMapper schoolMapper;
    private final MajorMapper majorMapper;
    private final ClazzMapper clazzMapper;
    private final ComAssessActivityMapper activityMapper;
    private final ComAssessExtraItemMapper extraItemMapper;
    private final ComAssessScoreMapper scoreMapper;
    private final PenaltyRecordMapper penaltyMapper;
    private final MessageNotificationMapper notificationMapper;
    private final ActivityService activityService;
    private final StudentService studentService;
    private final NotifyService notifyService;

    // ============ 学生管理 ============

    /** 条件分页查询学生 */
    public PageResult<Student> pageStudents(long page, long size, String name, String username, String phone,
                                            Long clazzId, Long schoolId, Integer status, Integer role) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Student::getName, name);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(Student::getUsername, username);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(Student::getPhone, phone);
        }
        if (clazzId != null) {
            wrapper.eq(Student::getClazzId, clazzId);
        }
        if (schoolId != null) {
            wrapper.eq(Student::getSchoolId, schoolId);
        }
        if (status != null) {
            wrapper.eq(Student::getStatus, status);
        }
        if (role != null) {
            wrapper.eq(Student::getRole, role);
        }
        wrapper.orderByDesc(Student::getCreateTime);
        Page<Student> result = studentMapper.selectPage(new Page<>(page, size), wrapper);
        enrichStudents(result.getRecords());
        return PageResult.of(result);
    }

    /** 查看学生详情 */
    public Student getStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        studentService.enrichStudent(student);
        student.setPassword(null);
        return student;
    }

    /** 新增学生（预置账号，默认密码为身份证后6位或时间） */
    @Transactional(rollbackFor = Exception.class)
    public void addStudent(Student student, List<Long> majorIds) {
        Long count = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getUsername, student.getUsername()));
        if (count > 0) {
            throw new BusinessException("学号已存在");
        }
        validateStudentRef(student, majorIds);
        if (student.getStatus() == null) {
            student.setStatus(Constants.STUDENT_STATUS_IN);
        }
        if (student.getRole() == null) {
            student.setRole(Constants.STUDENT_ROLE_NORMAL);
        }
        // 默认密码：身份证后6位，无身份证用当前时间数字
        student.setPassword(PasswordUtil.encode(PasswordUtil.defaultPassword(student.getNumber())));
        studentMapper.insert(student);
        syncStudentMajors(student.getId(), majorIds);
    }

    /** 修改学生信息（全部信息） */
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(Student student, List<Long> majorIds) {
        Student db = studentMapper.selectById(student.getId());
        if (db == null) {
            throw new BusinessException("学生不存在");
        }
        // 学号不允许与其它学生重复
        if (student.getUsername() != null && !student.getUsername().isBlank()) {
            Long dup = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                    .eq(Student::getUsername, student.getUsername())
                    .ne(Student::getId, student.getId()));
            if (dup != null && dup > 0) {
                throw new BusinessException("学号已存在");
            }
        }
        validateStudentRef(student, majorIds);
        // 若传入了明文密码则更新密码
        if (student.getPassword() != null && !student.getPassword().isEmpty()) {
            student.setPassword(PasswordUtil.encode(student.getPassword()));
        } else {
            student.setPassword(null); // 不修改密码
        }
        studentMapper.updateById(student);
        if (majorIds != null) {
            syncStudentMajors(student.getId(), majorIds);
        }
    }

    /** 删除学生 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        studentMapper.deleteById(id);
        studentMajorMapper.delete(new LambdaQueryWrapper<StudentMajor>()
                .eq(StudentMajor::getStudentId, id));
    }

    /** 重置学生密码为默认密码 */
    public void resetStudentPassword(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        student.setPassword(PasswordUtil.encode(PasswordUtil.defaultPassword(student.getNumber())));
        studentMapper.updateById(student);
    }

    // ============ 员工管理 ============

    public PageResult<Employee> pageEmployees(long page, long size, String name, String username, String phone,
                                              Integer role, Integer status) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Employee::getName, name);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(Employee::getUsername, username);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(Employee::getPhone, phone);
        }
        if (role != null) {
            wrapper.eq(Employee::getRole, role);
        }
        if (status != null) {
            wrapper.eq(Employee::getStatus, status);
        }
        wrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> result = employeeMapper.selectPage(new Page<>(page, size), wrapper);
        enrichEmployees(result.getRecords());
        return PageResult.of(result);
    }

    public Employee getEmployee(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        enrichEmployee(employee);
        employee.setPassword(null);
        return employee;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addEmployee(Employee employee, List<Long> schoolIds) {
        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employee.getUsername()));
        if (count > 0) {
            throw new BusinessException("工号已存在");
        }
        // 校验关联的班级/学院是否存在
        requireClazz(employee.getClazzId());
        if (schoolIds != null) {
            for (Long schoolId : schoolIds) {
                requireSchool(schoolId);
            }
        }
        if (employee.getStatus() == null) {
            employee.setStatus(Constants.EMPLOYEE_STATUS_ON);
        }
        if (employee.getRole() == null) {
            employee.setRole(Constants.EMPLOYEE_ROLE_TEACHER);
        }
        employee.setPassword(PasswordUtil.encode(PasswordUtil.defaultPassword(employee.getNumber())));
        employeeMapper.insert(employee);
        syncEmployeeSchools(employee.getId(), schoolIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(Employee employee, List<Long> schoolIds) {
        Employee db = employeeMapper.selectById(employee.getId());
        if (db == null) {
            throw new BusinessException("员工不存在");
        }
        // 工号不允许与其它员工重复
        if (employee.getUsername() != null && !employee.getUsername().isBlank()) {
            Long dup = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getUsername, employee.getUsername())
                    .ne(Employee::getId, employee.getId()));
            if (dup != null && dup > 0) {
                throw new BusinessException("工号已存在");
            }
        }
        // 校验关联的班级/学院是否存在
        requireClazz(employee.getClazzId());
        if (schoolIds != null) {
            for (Long schoolId : schoolIds) {
                requireSchool(schoolId);
            }
        }
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            employee.setPassword(PasswordUtil.encode(employee.getPassword()));
        } else {
            employee.setPassword(null);
        }
        employeeMapper.updateById(employee);
        if (schoolIds != null) {
            syncEmployeeSchools(employee.getId(), schoolIds);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long id) {
        employeeMapper.deleteById(id);
        employeeSchoolMapper.delete(new LambdaQueryWrapper<EmployeeSchool>()
                .eq(EmployeeSchool::getEmployeeId, id));
    }

    // ============ 学院 / 专业 / 班级管理 ============

    public PageResult<School> pageSchools(long page, long size, String name, Integer status) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(School::getName, name);
        }
        if (status != null) {
            wrapper.eq(School::getStatus, status);
        }
        return PageResult.of(schoolMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public void addSchool(School school) {
        if (school.getStatus() == null) {
            school.setStatus(Constants.SCHOOL_STATUS_NORMAL);
        }
        schoolMapper.insert(school);
    }

    public void updateSchool(School school) {
        schoolMapper.updateById(school);
    }

    public void deleteSchool(Long id) {
        schoolMapper.deleteById(id);
    }

    public PageResult<Major> pageMajors(long page, long size, String name, Long schoolId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Major::getName, name);
        }
        if (schoolId != null) {
            wrapper.eq(Major::getSchoolId, schoolId);
        }
        Page<Major> result = majorMapper.selectPage(new Page<>(page, size), wrapper);
        enrichMajors(result.getRecords());
        return PageResult.of(result);
    }

    public void addMajor(Major major) {
        requireSchool(major.getSchoolId());
        majorMapper.insert(major);
    }

    public void updateMajor(Major major) {
        requireSchool(major.getSchoolId());
        majorMapper.updateById(major);
    }

    public void deleteMajor(Long id) {
        majorMapper.deleteById(id);
    }

    public PageResult<Clazz> pageClazzs(long page, long size, String name, Long schoolId, Long majorId, Integer status) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Clazz::getName, name);
        }
        if (schoolId != null) {
            wrapper.eq(Clazz::getSchoolId, schoolId);
        }
        if (majorId != null) {
            wrapper.eq(Clazz::getMajorId, majorId);
        }
        if (status != null) {
            wrapper.eq(Clazz::getStatus, status);
        }
        Page<Clazz> result = clazzMapper.selectPage(new Page<>(page, size), wrapper);
        enrichClazzs(result.getRecords());
        return PageResult.of(result);
    }

    public void addClazz(Clazz clazz) {
        validateClazzRef(clazz);
        if (clazz.getStatus() == null) {
            clazz.setStatus(Constants.CLAZZ_STATUS_IN);
        }
        clazzMapper.insert(clazz);
    }

    public void updateClazz(Clazz clazz) {
        validateClazzRef(clazz);
        clazzMapper.updateById(clazz);
    }

    public void deleteClazz(Long id) {
        clazzMapper.deleteById(id);
    }

    // ============ 下拉选项（新增/编辑表单回显用，仅返回基础字段） ============

    /** 学院下拉选项：全部正常学院 */
    public List<School> listSchoolOptions() {
        return schoolMapper.selectList(new LambdaQueryWrapper<School>()
                .eq(School::getStatus, Constants.SCHOOL_STATUS_NORMAL)
                .orderByAsc(School::getId));
    }

    /** 专业下拉选项（可按学院过滤） */
    public List<Major> listMajorOptions(Long schoolId) {
        return majorMapper.selectList(new LambdaQueryWrapper<Major>()
                .eq(schoolId != null, Major::getSchoolId, schoolId)
                .orderByAsc(Major::getId));
    }

    /** 班级下拉选项（可按学院/专业过滤，仅在用班级） */
    public List<Clazz> listClazzOptions(Long schoolId, Long majorId) {
        return clazzMapper.selectList(new LambdaQueryWrapper<Clazz>()
                .eq(Clazz::getStatus, Constants.CLAZZ_STATUS_IN)
                .eq(schoolId != null, Clazz::getSchoolId, schoolId)
                .eq(majorId != null, Clazz::getMajorId, majorId)
                .orderByAsc(Clazz::getId));
    }

    // ============ 活动管理 ============

    public PageResult<ComAssessActivity> pageActivities(long page, long size, String name,
                                                        Integer type, Integer status, Long schoolId) {
        LambdaQueryWrapper<ComAssessActivity> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(ComAssessActivity::getName, name);
        }
        if (type != null) {
            wrapper.eq(ComAssessActivity::getType, type);
        }
        if (status != null) {
            wrapper.eq(ComAssessActivity::getStatus, status);
        }
        if (schoolId != null) {
            wrapper.eq(ComAssessActivity::getSchoolId, schoolId);
        }
        wrapper.orderByDesc(ComAssessActivity::getCreateTime);
        Page<ComAssessActivity> result = activityMapper.selectPage(new Page<>(page, size), wrapper);
        activityService.enrichList(result.getRecords(), null);
        return PageResult.of(result);
    }

    public ComAssessActivity getActivity(Long id) {
        ComAssessActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activityService.enrich(activity, null);
        return activity;
    }

    public void updateActivity(ComAssessActivity activity) {
        requireSchool(activity.getSchoolId());
        activityMapper.updateById(activity);
    }

    /** 审核活动 */
    public void reviewActivity(Long id, ReviewDTO dto, Long reviewerId) {
        activityService.review(id, dto, reviewerId);
    }

    // ============ 加分项审核 ============

    public PageResult<ComAssessExtraItem> pageExtraItems(long page, long size, Integer status, Long studentId,
                                                         String name, String reason) {
        LambdaQueryWrapper<ComAssessExtraItem> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ComAssessExtraItem::getStatus, status);
        }
        if (studentId != null) {
            wrapper.eq(ComAssessExtraItem::getStudentId, studentId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(ComAssessExtraItem::getName, name);
        }
        if (reason != null && !reason.isEmpty()) {
            wrapper.like(ComAssessExtraItem::getReason, reason);
        }
        wrapper.orderByDesc(ComAssessExtraItem::getCreateTime);
        Page<ComAssessExtraItem> result = extraItemMapper.selectPage(new Page<>(page, size), wrapper);
        fillExtraItems(result.getRecords());
        return PageResult.of(result);
    }

    /** 审核加分项（通用） */
    @Transactional(rollbackFor = Exception.class)
    public void reviewExtraItem(Long id, ReviewDTO dto, Long reviewerId) {
        ComAssessExtraItem item = extraItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("加分项不存在");
        }
        if (Boolean.TRUE.equals(dto.getApprove())) {
            item.setStatus(Constants.AUDIT_STATUS_APPROVED);
        } else {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new BusinessException("审核不通过需填写原因");
            }
            item.setStatus(Constants.AUDIT_STATUS_REJECTED);
            item.setReason(dto.getReason());
        }
        item.setReviewerId(reviewerId);
        extraItemMapper.updateById(item);

        // 通知学生
        String content = Boolean.TRUE.equals(dto.getApprove())
                ? "你的加分项「" + item.getName() + "」已审核通过。"
                : "你的加分项「" + item.getName() + "」未通过，原因：" + dto.getReason();
        notifyService.send(item.getStudentId(), Constants.RECEIVER_STUDENT,
                "加分项审核结果", content, 2, item.getId());
    }

    /** 查询自己班级学生的加分项（辅导员） */
    public PageResult<ComAssessExtraItem> pageMyClassExtraItems(long page, long size, Integer status,
                                                                String name, String reason, Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null || employee.getClazzId() == null) {
            throw new BusinessException("当前员工未关联班级");
        }
        List<Long> studentIds = studentMapper.selectIdsByClazz(employee.getClazzId());
        if (studentIds.isEmpty()) {
            return new PageResult<>(0, page, size, List.of());
        }
        LambdaQueryWrapper<ComAssessExtraItem> wrapper = new LambdaQueryWrapper<ComAssessExtraItem>()
                .in(ComAssessExtraItem::getStudentId, studentIds);
        if (status != null) {
            wrapper.eq(ComAssessExtraItem::getStatus, status);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(ComAssessExtraItem::getName, name);
        }
        if (reason != null && !reason.isEmpty()) {
            wrapper.like(ComAssessExtraItem::getReason, reason);
        }
        wrapper.orderByDesc(ComAssessExtraItem::getCreateTime);
        Page<ComAssessExtraItem> result = extraItemMapper.selectPage(new Page<>(page, size), wrapper);
        fillExtraItems(result.getRecords());
        return PageResult.of(result);
    }

    /** 审核自己班级学生的加分项 */
    public void reviewMyClassExtraItem(Long id, ReviewDTO dto, Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null || employee.getClazzId() == null) {
            throw new BusinessException("当前员工未关联班级");
        }
        ComAssessExtraItem item = extraItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("加分项不存在");
        }
        Student student = studentMapper.selectById(item.getStudentId());
        if (student == null || !Objects.equals(student.getClazzId(), employee.getClazzId())) {
            throw new BusinessException("无权审核该学生的加分项");
        }
        reviewExtraItem(id, dto, employeeId);
    }

    // ============ 违规记录 ============

    public void addPenalty(PenaltyAddDTO dto) {
        Student student = studentMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        PenaltyRecord penalty = new PenaltyRecord();
        penalty.setStudentId(dto.getStudentId());
        penalty.setName(dto.getName());
        penalty.setReason(dto.getReason());
        penalty.setScore(dto.getScore());
        penalty.setPunishment(dto.getPunishment());
        penaltyMapper.insert(penalty);

        notifyService.send(dto.getStudentId(), Constants.RECEIVER_STUDENT,
                "违规记录提醒", "你有新的违规记录：「" + dto.getName() + "」，扣分 " + dto.getScore() + " 分。",
                3, penalty.getId());
    }

    public PageResult<PenaltyRecord> pagePenalties(long page, long size, Long studentId, String name, String reason) {
        LambdaQueryWrapper<PenaltyRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(PenaltyRecord::getStudentId, studentId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(PenaltyRecord::getName, name);
        }
        if (reason != null && !reason.isEmpty()) {
            wrapper.like(PenaltyRecord::getReason, reason);
        }
        wrapper.orderByDesc(PenaltyRecord::getCreateTime);
        return PageResult.of(penaltyMapper.selectPage(new Page<>(page, size), wrapper));
    }

    // ============ 综测成绩审核与查询 ============

    /** 查询所有已审核通过的综测成绩（条件分页） */
    public PageResult<ComAssessScore> pageApprovedScores(long page, long size, Long studentId, Integer year, Integer semester) {
        LambdaQueryWrapper<ComAssessScore> wrapper = new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_APPROVED);
        if (studentId != null) {
            wrapper.eq(ComAssessScore::getStudentId, studentId);
        }
        if (year != null) {
            wrapper.eq(ComAssessScore::getYear, year);
        }
        if (semester != null) {
            wrapper.eq(ComAssessScore::getSemester, semester);
        }
        wrapper.orderByDesc(ComAssessScore::getCreateTime);
        Page<ComAssessScore> result = scoreMapper.selectPage(new Page<>(page, size), wrapper);
        fillScores(result.getRecords());
        return PageResult.of(result);
    }

    /** 查询待审核综测（供审核列表） */
    public PageResult<ComAssessScore> pagePendingScores(long page, long size, Long studentId, Integer year, Integer semester) {
        LambdaQueryWrapper<ComAssessScore> wrapper = new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_UNAUDITED);
        if (studentId != null) {
            wrapper.eq(ComAssessScore::getStudentId, studentId);
        }
        if (year != null) {
            wrapper.eq(ComAssessScore::getYear, year);
        }
        if (semester != null) {
            wrapper.eq(ComAssessScore::getSemester, semester);
        }
        wrapper.orderByDesc(ComAssessScore::getCreateTime);
        Page<ComAssessScore> result = scoreMapper.selectPage(new Page<>(page, size), wrapper);
        fillScores(result.getRecords());
        return PageResult.of(result);
    }

    /** 批量补全加分项记录的学生学号/姓名（审核列表展示用） */
    private void fillExtraItems(List<ComAssessExtraItem> items) {
        Map<Long, Student> map = studentMapByIds(
                items.stream().map(ComAssessExtraItem::getStudentId).collect(Collectors.toList()));
        items.forEach(item -> {
            Student s = map.get(item.getStudentId());
            if (s != null) {
                item.setStudentUsername(s.getUsername());
                item.setStudentName(s.getName());
            }
        });
    }

    /** 批量补全综测成绩记录的学生学号/姓名（审核列表展示用） */
    private void fillScores(List<ComAssessScore> scores) {
        Map<Long, Student> map = studentMapByIds(
                scores.stream().map(ComAssessScore::getStudentId).collect(Collectors.toList()));
        scores.forEach(score -> {
            Student s = map.get(score.getStudentId());
            if (s != null) {
                score.setStudentUsername(s.getUsername());
                score.setStudentName(s.getName());
            }
        });
    }

    /** 学生 id -> 学生 映射（入参为空时返回空表） */
    private Map<Long, Student> studentMapByIds(List<Long> ids) {
        List<Long> valid = ids.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (valid.isEmpty()) {
            return Map.of();
        }
        Map<Long, Student> map = new HashMap<>();
        for (Student s : studentMapper.selectBatchIds(valid)) {
            map.put(s.getId(), s);
        }
        return map;
    }

    /** 审核综测成绩（通过即生效） */
    @Transactional(rollbackFor = Exception.class)
    public void reviewScore(Long id, ReviewDTO dto, Long reviewerId) {
        ComAssessScore score = scoreMapper.selectById(id);
        if (score == null) {
            throw new BusinessException("综测成绩不存在");
        }
        if (Boolean.TRUE.equals(dto.getApprove())) {
            // 一年最多 2 个通过，硬拦截
            Long approvedCount = scoreMapper.selectCount(new LambdaQueryWrapper<ComAssessScore>()
                    .eq(ComAssessScore::getStudentId, score.getStudentId())
                    .eq(ComAssessScore::getYear, score.getYear())
                    .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_APPROVED));
            if (approvedCount != null && approvedCount >= Constants.SCORE_MAX_APPROVED_PER_YEAR) {
                throw new BusinessException("该学生本年已通过 " + Constants.SCORE_MAX_APPROVED_PER_YEAR + " 个综测，无法再通过");
            }
            score.setStatus(Constants.AUDIT_STATUS_APPROVED);
        } else {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new BusinessException("审核不通过需填写原因");
            }
            score.setStatus(Constants.AUDIT_STATUS_REJECTED);
            score.setReason(dto.getReason());
        }
        score.setReviewerId(reviewerId);
        scoreMapper.updateById(score);

        String content = Boolean.TRUE.equals(dto.getApprove())
                ? "你的综测成绩（" + score.getYear() + " 年）已审核通过，总成绩 " + score.getScore() + " 分。"
                : "你的综测成绩（" + score.getYear() + " 年）未通过，原因：" + dto.getReason();
        notifyService.send(score.getStudentId(), Constants.RECEIVER_STUDENT,
                "综测成绩审核结果", content, 4, score.getId());
    }

    // ============ 统计 ============

    /** 统计信息：学院/专业/班级均分、板块对比、活动统计 */
    public Map<String, Object> statistics() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 学院均分
        List<Map<String, Object>> schoolAverages = new ArrayList<>();
        for (School school : schoolMapper.selectList(null)) {
            BigDecimal avg = scoreMapper.avgBySchool(school.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", school.getId());
            row.put("name", school.getName());
            row.put("avg", avg == null ? null : avg.setScale(2, RoundingMode.HALF_UP));
            schoolAverages.add(row);
        }
        result.put("schoolAverages", schoolAverages);

        // 专业均分
        List<Map<String, Object>> majorAverages = new ArrayList<>();
        for (Major major : majorMapper.selectList(null)) {
            BigDecimal avg = scoreMapper.avgByMajor(major.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", major.getId());
            row.put("name", major.getName());
            row.put("avg", avg == null ? null : avg.setScale(2, RoundingMode.HALF_UP));
            majorAverages.add(row);
        }
        result.put("majorAverages", majorAverages);

        // 班级均分
        List<Map<String, Object>> clazzAverages = new ArrayList<>();
        for (Clazz clazz : clazzMapper.selectList(null)) {
            BigDecimal avg = scoreMapper.avgByClazz(clazz.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", clazz.getId());
            row.put("name", clazz.getName());
            row.put("avg", avg == null ? null : avg.setScale(2, RoundingMode.HALF_UP));
            clazzAverages.add(row);
        }
        result.put("clazzAverages", clazzAverages);

        // 板块对比（所有已审核综测的各板块平均分）
        List<ComAssessScore> approvedScores = scoreMapper.selectList(new LambdaQueryWrapper<ComAssessScore>()
                .eq(ComAssessScore::getStatus, Constants.AUDIT_STATUS_APPROVED));
        Map<String, Object> board = new LinkedHashMap<>();
        board.put("活动平均分", avg(approvedScores.stream().map(ComAssessScore::getActivityScore).toList()));
        board.put("加分平均分", avg(approvedScores.stream().map(ComAssessScore::getExtraScore).toList()));
        board.put("扣分平均分", avg(approvedScores.stream().map(ComAssessScore::getPenaltyScore).toList()));
        board.put("总平均分", avg(approvedScores.stream().map(ComAssessScore::getScore).toList()));
        result.put("boardComparison", board);

        // 活动统计
        List<ComAssessActivity> activities = activityMapper.selectList(null);
        Map<String, Object> activityStats = new LinkedHashMap<>();
        activityStats.put("活动总数", activities.size());
        activityStats.put("未审核", activities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_UNAUDITED).count());
        activityStats.put("审核通过", activities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_APPROVED).count());
        activityStats.put("审核不通过", activities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_REJECTED).count());
        activityStats.put("已结束", activities.stream().filter(a -> a.getStatus() == Constants.ACTIVITY_STATUS_FINISHED).count());
        activityStats.put("校级活动", activities.stream().filter(a -> a.getType() == 1 || a.getType() == 2).count());
        activityStats.put("院级活动", activities.stream().filter(a -> a.getType() == 3 || a.getType() == 4).count());
        result.put("activityStats", activityStats);

        return result;
    }

    // ============ 通知 ============

    public PageResult<MessageNotification> pageNotifications(long page, long size, Long employeeId) {
        Page<MessageNotification> result = notificationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MessageNotification>()
                        .eq(MessageNotification::getReceiverId, employeeId)
                        .eq(MessageNotification::getReceiverType, Constants.RECEIVER_EMPLOYEE)
                        .orderByDesc(MessageNotification::getCreateTime));
        return PageResult.of(result);
    }

    public void markNotificationRead(Long id, Long employeeId) {
        MessageNotification notification = notificationMapper.selectById(id);
        if (notification == null || !notification.getReceiverId().equals(employeeId)) {
            throw new BusinessException("通知不存在");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    // ============ 内部工具 ============

    /** 校验学生关联的班级/学院/专业是否存在且相互一致 */
    private void validateStudentRef(Student student, List<Long> majorIds) {
        Clazz clazz = requireClazz(student.getClazzId());
        if (clazz != null) {
            // 以班级为准自动校正学院，防止出现不一致
            if (student.getSchoolId() != null && !student.getSchoolId().equals(clazz.getSchoolId())) {
                throw new BusinessException("所选班级不属于该学院，请重新选择");
            }
            student.setSchoolId(clazz.getSchoolId());
        } else {
            requireSchool(student.getSchoolId());
        }
        if (majorIds != null) {
            for (Long majorId : majorIds) {
                Major major = requireMajor(majorId);
                if (student.getSchoolId() != null && major != null && major.getSchoolId() != null
                        && !major.getSchoolId().equals(student.getSchoolId())) {
                    throw new BusinessException("所选专业不属于该学院，请重新选择");
                }
            }
        }
    }

    /** 校验班级关联的学院/专业是否存在且相互一致 */
    private void validateClazzRef(Clazz clazz) {
        requireSchool(clazz.getSchoolId());
        Major major = requireMajor(clazz.getMajorId());
        if (major != null) {
            if (clazz.getSchoolId() == null) {
                // 未指定学院时按专业所属学院自动填充
                clazz.setSchoolId(major.getSchoolId());
            } else if (major.getSchoolId() != null && !major.getSchoolId().equals(clazz.getSchoolId())) {
                throw new BusinessException("所选专业不属于该学院，请重新选择");
            }
        }
    }

    /** 校验学院存在，返回实体 */
    private School requireSchool(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("所选学院不存在或已删除");
        }
        return school;
    }

    /** 校验专业存在，返回实体 */
    private Major requireMajor(Long majorId) {
        if (majorId == null) {
            return null;
        }
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException("所选专业不存在或已删除");
        }
        return major;
    }

    /** 校验班级存在，返回实体 */
    private Clazz requireClazz(Long clazzId) {
        if (clazzId == null) {
            return null;
        }
        Clazz clazz = clazzMapper.selectById(clazzId);
        if (clazz == null) {
            throw new BusinessException("所选班级不存在或已删除");
        }
        return clazz;
    }

    private void syncStudentMajors(Long studentId, List<Long> majorIds) {
        studentMajorMapper.delete(new LambdaQueryWrapper<StudentMajor>()
                .eq(StudentMajor::getStudentId, studentId));
        if (majorIds != null) {
            for (Long majorId : majorIds) {
                StudentMajor sm = new StudentMajor();
                sm.setStudentId(studentId);
                sm.setMajorId(majorId);
                studentMajorMapper.insert(sm);
            }
        }
    }

    private void syncEmployeeSchools(Long employeeId, List<Long> schoolIds) {
        employeeSchoolMapper.delete(new LambdaQueryWrapper<EmployeeSchool>()
                .eq(EmployeeSchool::getEmployeeId, employeeId));
        if (schoolIds != null) {
            for (Long schoolId : schoolIds) {
                EmployeeSchool es = new EmployeeSchool();
                es.setEmployeeId(employeeId);
                es.setSchoolId(schoolId);
                employeeSchoolMapper.insert(es);
            }
        }
    }

    private void enrichStudents(List<Student> students) {
        for (Student student : students) {
            studentService.enrichStudent(student);
            student.setPassword(null);
        }
    }

    private void enrichEmployee(Employee employee) {
        if (employee.getClazzId() != null) {
            Clazz clazz = clazzMapper.selectById(employee.getClazzId());
            employee.setClazzName(clazz == null ? null : clazz.getName());
        }
        List<Long> schoolIds = employeeSchoolMapper.selectList(new LambdaQueryWrapper<EmployeeSchool>()
                        .eq(EmployeeSchool::getEmployeeId, employee.getId()))
                .stream().map(EmployeeSchool::getSchoolId).toList();
        employee.setSchoolIds(schoolIds);
        if (!schoolIds.isEmpty()) {
            employee.setSchoolNames(schoolMapper.selectBatchIds(schoolIds).stream()
                    .map(School::getName).collect(Collectors.joining("、")));
        }
    }

    private void enrichEmployees(List<Employee> employees) {
        for (Employee employee : employees) {
            enrichEmployee(employee);
            employee.setPassword(null);
        }
    }

    private void enrichMajors(List<Major> majors) {
        for (Major major : majors) {
            if (major.getSchoolId() != null) {
                School school = schoolMapper.selectById(major.getSchoolId());
                major.setSchoolName(school == null ? null : school.getName());
            }
        }
    }

    private void enrichClazzs(List<Clazz> clazzs) {
        for (Clazz clazz : clazzs) {
            if (clazz.getSchoolId() != null) {
                School school = schoolMapper.selectById(clazz.getSchoolId());
                clazz.setSchoolName(school == null ? null : school.getName());
            }
            if (clazz.getMajorId() != null) {
                Major major = majorMapper.selectById(clazz.getMajorId());
                clazz.setMajorName(major == null ? null : major.getName());
            }
        }
    }

    private BigDecimal avg(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = values.stream().filter(Objects::nonNull).count();
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
