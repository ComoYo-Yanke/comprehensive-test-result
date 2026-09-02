package com.zongce.comprehensive.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.entity.Student;
import com.zongce.comprehensive.mapper.StudentMapper;
import com.zongce.comprehensive.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 综测提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreRemindTask {

    private final StudentMapper studentMapper;
    private final NotifyService notifyService;

    /**
     * 每年 3 月 1 日、9 月 1 日 9:00：提醒所有在读学生计算综测成绩。
     */
    @Scheduled(cron = "0 0 9 1 3,9 *")
    public void remindAllStudents() {
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getStatus, Constants.STUDENT_STATUS_IN));
        for (Student student : students) {
            notifyService.send(student.getId(), Constants.RECEIVER_STUDENT,
                    "综测成绩计算提醒", "期末已至，请及时计算并提交你的综测成绩。", 4, null);
        }
        log.info("已向 {} 名在读学生发送综测计算提醒", students.size());
    }
}
