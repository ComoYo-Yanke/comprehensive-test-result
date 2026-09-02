package com.zongce.comprehensive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.ActivityCreateDTO;
import com.zongce.comprehensive.dto.ReviewDTO;
import com.zongce.comprehensive.entity.ActivityChargeStudent;
import com.zongce.comprehensive.entity.ActivityEmployee;
import com.zongce.comprehensive.entity.ActivityStudent;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.School;
import com.zongce.comprehensive.mapper.ActivityChargeStudentMapper;
import com.zongce.comprehensive.mapper.ActivityEmployeeMapper;
import com.zongce.comprehensive.mapper.ActivityStudentMapper;
import com.zongce.comprehensive.mapper.ComAssessActivityMapper;
import com.zongce.comprehensive.mapper.SchoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动服务：创建、报名（分布式锁防并发）、审核、信息填充等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ComAssessActivityMapper activityMapper;
    private final ActivityStudentMapper activityStudentMapper;
    private final ActivityEmployeeMapper activityEmployeeMapper;
    private final ActivityChargeStudentMapper activityChargeStudentMapper;
    private final SchoolMapper schoolMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final NotifyService notifyService;

    /**
     * 学生申请创建活动（学生会/社团成员），并通知负责老师
     */
    @Transactional(rollbackFor = Exception.class)
    public ComAssessActivity create(ActivityCreateDTO dto, Long studentId) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        ComAssessActivity activity = new ComAssessActivity();
        activity.setName(dto.getName());
        activity.setType(dto.getType());
        activity.setSchoolId(dto.getSchoolId());
        activity.setLimitNum(dto.getLimitNum());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setDescription(dto.getDescription());
        // 新创建的活动默认未审核
        activity.setStatus(Constants.ACTIVITY_STATUS_UNAUDITED);
        activityMapper.insert(activity);

        // 负责老师关系
        if (dto.getEmpInChargeIds() != null) {
            for (Long empId : dto.getEmpInChargeIds()) {
                ActivityEmployee ae = new ActivityEmployee();
                ae.setActivityId(activity.getId());
                ae.setEmployeeId(empId);
                activityEmployeeMapper.insert(ae);
            }
        }
        // 负责学生关系
        if (dto.getStudentInChargeIds() != null) {
            for (Long sid : dto.getStudentInChargeIds()) {
                ActivityChargeStudent acs = new ActivityChargeStudent();
                acs.setActivityId(activity.getId());
                acs.setStudentId(sid);
                activityChargeStudentMapper.insert(acs);
            }
        }

        // 通知负责老师
        if (dto.getEmpInChargeIds() != null) {
            for (Long empId : dto.getEmpInChargeIds()) {
                notifyService.send(empId, Constants.RECEIVER_EMPLOYEE,
                        "新的活动申请", "有学生申请创建活动「" + activity.getName() + "」，请及时审核。",
                        1, activity.getId());
            }
        }
        return activity;
    }

    /**
     * 学生报名活动（Redis 分布式锁防并发超员）
     */
    public void join(Long activityId, Long studentId) {
        String lockKey = "lock:activity:join:" + activityId;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("报名人数较多，请稍后重试");
        }
        try {
            ComAssessActivity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new BusinessException("活动不存在");
            }
            // 只有审核通过或举办中的活动才可报名
            if (activity.getStatus() != Constants.ACTIVITY_STATUS_APPROVED
                    && activity.getStatus() != Constants.ACTIVITY_STATUS_HOLDING) {
                throw new BusinessException("该活动当前不可报名");
            }
            // 是否已参加
            Long already = activityStudentMapper.selectCount(new LambdaQueryWrapper<ActivityStudent>()
                    .eq(ActivityStudent::getActivityId, activityId)
                    .eq(ActivityStudent::getStudentId, studentId));
            if (already != null && already > 0) {
                throw new BusinessException("你已参加该活动，无法重复报名");
            }
            // 是否满员
            long count = activityStudentMapper.countByActivity(activityId);
            if (activity.getLimitNum() != null && count >= activity.getLimitNum()) {
                throw new BusinessException("活动已满员，无法加入");
            }
            // 插入报名记录（joinTime 毫秒精度，用于踢人排序）
            ActivityStudent record = new ActivityStudent();
            record.setActivityId(activityId);
            record.setStudentId(studentId);
            record.setJoinTime(LocalDateTime.now());
            activityStudentMapper.insert(record);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 员工审核活动
     */
    @Transactional(rollbackFor = Exception.class)
    public void review(Long activityId, ReviewDTO dto, Long reviewerId) {
        ComAssessActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (Boolean.TRUE.equals(dto.getApprove())) {
            activity.setStatus(Constants.ACTIVITY_STATUS_APPROVED);
        } else {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                throw new BusinessException("审核不通过需填写原因");
            }
            activity.setStatus(Constants.ACTIVITY_STATUS_REJECTED);
        }
        activityMapper.updateById(activity);

        // 通知活动发起人（创建学生）
        if (activity.getCreateUser() != null) {
            String content = Boolean.TRUE.equals(dto.getApprove())
                    ? "你申请的活动「" + activity.getName() + "」已审核通过。"
                    : "你申请的活动「" + activity.getName() + "」未通过，原因：" + dto.getReason();
            notifyService.send(activity.getCreateUser(), Constants.RECEIVER_STUDENT,
                    "活动审核结果", content, 1, activity.getId());
        }
    }

    /**
     * 为单个活动填充展示字段（学院名、参加人数、是否参加、可得综测分）
     */
    public void enrich(ComAssessActivity activity, Long studentId) {
        if (activity == null) {
            return;
        }
        if (activity.getSchoolId() != null) {
            School school = schoolMapper.selectById(activity.getSchoolId());
            activity.setSchoolName(school == null ? null : school.getName());
        }
        long count = activityStudentMapper.countByActivity(activity.getId());
        activity.setJoinedCount(count);

        boolean joined = false;
        if (studentId != null) {
            Long cnt = activityStudentMapper.selectCount(new LambdaQueryWrapper<ActivityStudent>()
                    .eq(ActivityStudent::getActivityId, activity.getId())
                    .eq(ActivityStudent::getStudentId, studentId));
            joined = cnt != null && cnt > 0;
        }
        activity.setJoined(joined);
        // 可得综测分：校级 0.2，院级 0.1；未参加则 0
        if (joined) {
            double score = (activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_THOUGHT
                    || activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_SPORT)
                    ? Constants.SCORE_SCHOOL_ACTIVITY : Constants.SCORE_COLLEGE_ACTIVITY;
            activity.setMyScore(BigDecimal.valueOf(score));
        } else {
            activity.setMyScore(BigDecimal.ZERO);
        }
    }

    /**
     * 批量填充活动展示字段（批量查学院名，避免 N+1）
     */
    public void enrichList(List<ComAssessActivity> list, Long studentId) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> schoolIds = list.stream().map(ComAssessActivity::getSchoolId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> schoolNameMap = schoolIds.isEmpty() ? Map.of()
                : schoolMapper.selectBatchIds(schoolIds).stream()
                .collect(Collectors.toMap(School::getId, School::getName, (a, b) -> a));

        for (ComAssessActivity activity : list) {
            activity.setSchoolName(schoolNameMap.get(activity.getSchoolId()));
            long count = activityStudentMapper.countByActivity(activity.getId());
            activity.setJoinedCount(count);
        }

        // 批量判断当前学生已参加的活动
        if (studentId != null) {
            List<Long> joinedIds = activityStudentMapper.selectList(
                            new LambdaQueryWrapper<ActivityStudent>()
                                    .eq(ActivityStudent::getStudentId, studentId))
                    .stream().map(ActivityStudent::getActivityId).toList();
            Set<Long> joinedSet = Set.copyOf(joinedIds);
            for (ComAssessActivity activity : list) {
                boolean joined = joinedSet.contains(activity.getId());
                activity.setJoined(joined);
                if (joined) {
                    double score = (activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_THOUGHT
                            || activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_SPORT)
                            ? Constants.SCORE_SCHOOL_ACTIVITY : Constants.SCORE_COLLEGE_ACTIVITY;
                    activity.setMyScore(BigDecimal.valueOf(score));
                } else {
                    activity.setMyScore(BigDecimal.ZERO);
                }
            }
        }
    }

    /** 根据活动类型返回单活动分值（校级 0.2 / 院级 0.1） */
    public BigDecimal scoreOf(ComAssessActivity activity) {
        if (activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_THOUGHT
                || activity.getType() == Constants.ACTIVITY_TYPE_SCHOOL_SPORT) {
            return BigDecimal.valueOf(Constants.SCORE_SCHOOL_ACTIVITY);
        }
        return BigDecimal.valueOf(Constants.SCORE_COLLEGE_ACTIVITY);
    }

    /** 查询某学生已参加（且活动审核通过/举办中/已结束）的活动列表，用于综测计分 */
    public List<ComAssessActivity> listJoinedApproved(Long studentId) {
        List<Long> activityIds = activityStudentMapper.selectList(
                        new LambdaQueryWrapper<ActivityStudent>()
                                .eq(ActivityStudent::getStudentId, studentId))
                .stream().map(ActivityStudent::getActivityId).toList();
        if (activityIds.isEmpty()) {
            return List.of();
        }
        return activityMapper.selectList(new LambdaQueryWrapper<ComAssessActivity>()
                .in(ComAssessActivity::getId, activityIds)
                .in(ComAssessActivity::getStatus,
                        Constants.ACTIVITY_STATUS_APPROVED,
                        Constants.ACTIVITY_STATUS_HOLDING,
                        Constants.ACTIVITY_STATUS_FINISHED));
    }
}
