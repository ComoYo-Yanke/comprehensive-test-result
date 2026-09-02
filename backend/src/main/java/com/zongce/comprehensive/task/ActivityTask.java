package com.zongce.comprehensive.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.entity.ActivityStudent;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.Employee;
import com.zongce.comprehensive.mapper.ActivityStudentMapper;
import com.zongce.comprehensive.mapper.ComAssessActivityMapper;
import com.zongce.comprehensive.mapper.EmployeeMapper;
import com.zongce.comprehensive.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动相关定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityTask {

    private final ComAssessActivityMapper activityMapper;
    private final ActivityStudentMapper activityStudentMapper;
    private final EmployeeMapper employeeMapper;
    private final NotifyService notifyService;

    /**
     * 每分钟执行：活动超员时，踢出最后加入的学生（按毫秒），
     * 通知该学生并将异常警报给所有员工。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void kickOverload() {
        List<ComAssessActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<ComAssessActivity>()
                        .in(ComAssessActivity::getStatus,
                                Constants.ACTIVITY_STATUS_APPROVED, Constants.ACTIVITY_STATUS_HOLDING));
        for (ComAssessActivity activity : activities) {
            if (activity.getLimitNum() == null) {
                continue;
            }
            long count = activityStudentMapper.countByActivity(activity.getId());
            if (count <= activity.getLimitNum()) {
                continue;
            }
            long overflow = count - activity.getLimitNum();
            // 按加入时间倒序取最后加入的 overflow 条
            List<ActivityStudent> lastJoiners = activityStudentMapper.selectList(
                    new LambdaQueryWrapper<ActivityStudent>()
                            .eq(ActivityStudent::getActivityId, activity.getId())
                            .orderByDesc(ActivityStudent::getJoinTime)
                            .last("LIMIT " + overflow));
            for (ActivityStudent joiner : lastJoiners) {
                activityStudentMapper.deleteById(joiner.getId());
                notifyService.send(joiner.getStudentId(), Constants.RECEIVER_STUDENT,
                        "活动报名异常提醒", "活动「" + activity.getName() + "」报名人数超限，你已被移出，敬请谅解。",
                        1, activity.getId());
            }
            // 警报所有员工
            List<Employee> employees = employeeMapper.selectList(null);
            for (Employee employee : employees) {
                notifyService.send(employee.getId(), Constants.RECEIVER_EMPLOYEE,
                        "活动超员警报", "活动「" + activity.getName() + "」发生超员异常，已自动移除 "
                                + overflow + " 名最后加入的学生。", 1, activity.getId());
            }
            log.warn("活动[{}]超员，已移除 {} 名最后加入者", activity.getName(), overflow);
        }
    }

    /**
     * 每分钟执行：活动超过结束时间自动置为已结束。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void finishExpired() {
        activityMapper.update(null, new LambdaUpdateWrapper<ComAssessActivity>()
                .set(ComAssessActivity::getStatus, Constants.ACTIVITY_STATUS_FINISHED)
                .in(ComAssessActivity::getStatus,
                        Constants.ACTIVITY_STATUS_APPROVED, Constants.ACTIVITY_STATUS_HOLDING)
                .lt(ComAssessActivity::getEndTime, LocalDateTime.now()));
    }
}
