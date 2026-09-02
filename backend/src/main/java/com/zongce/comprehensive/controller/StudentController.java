package com.zongce.comprehensive.controller;

import com.zongce.comprehensive.common.PageResult;
import com.zongce.comprehensive.common.RateLimit;
import com.zongce.comprehensive.common.RequireRole;
import com.zongce.comprehensive.common.Result;
import com.zongce.comprehensive.common.UserContext;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.ActivityCreateDTO;
import com.zongce.comprehensive.dto.ExtraItemAddDTO;
import com.zongce.comprehensive.dto.SelfUpdateDTO;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.ComAssessExtraItem;
import com.zongce.comprehensive.entity.ComAssessScore;
import com.zongce.comprehensive.entity.MessageNotification;
import com.zongce.comprehensive.entity.PenaltyRecord;
import com.zongce.comprehensive.entity.Student;
import com.zongce.comprehensive.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学生端接口
 */
@Tag(name = "学生端接口")
@RestController
@RequestMapping("/api/v1/student")
@RequireRole(Constants.TYPE_STUDENT)
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ===== 个人信息 =====

    @Operation(summary = "查询个人信息")
    @GetMapping("/profile")
    public Result<Student> profile() {
        return Result.success(studentService.getProfile(UserContext.getUserId()));
    }

    @Operation(summary = "修改个人信息（电话/邮箱/密码/描述）")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SelfUpdateDTO dto) {
        studentService.updateSelf(UserContext.getUserId(), dto);
        return Result.success();
    }

    // ===== 活动 =====

    @Operation(summary = "活动列表（多条件筛选+分页）")
    @GetMapping("/activities")
    public Result<PageResult<ComAssessActivity>> activities(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer full,
            @RequestParam(required = false) Integer joined,
            @RequestParam(required = false) Integer minNum,
            @RequestParam(required = false) Integer maxNum) {
        return Result.success(studentService.pageActivities(page, size, type, schoolId, name,
                status, full, joined, minNum, maxNum, UserContext.getUserId()));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/activities/{id}")
    public Result<ComAssessActivity> activityDetail(@PathVariable Long id) {
        return Result.success(studentService.getActivityDetail(id, UserContext.getUserId()));
    }

    @Operation(summary = "报名活动")
    @PostMapping("/activities/{id}/join")
    public Result<Void> joinActivity(@PathVariable Long id) {
        studentService.joinActivity(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "申请创建活动（学生会/社团成员）")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 10, windowSeconds = 60)
    @PostMapping("/activities")
    public Result<ComAssessActivity> createActivity(@Valid @RequestBody ActivityCreateDTO dto) {
        return Result.success(studentService.createActivity(dto, UserContext.getUserId()));
    }

    @Operation(summary = "创建活动弹窗下拉数据（学院/负责老师）")
    @GetMapping("/activity/options")
    public Result<Map<String, Object>> activityOptions() {
        return Result.success(studentService.activityOptions());
    }

    // ===== 其他综测加分 =====

    @Operation(summary = "添加其他综测加分项")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 10, windowSeconds = 60)
    @PostMapping("/extra-items")
    public Result<Void> addExtraItem(@Valid @RequestBody ExtraItemAddDTO dto) {
        studentService.addExtraItem(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "查询自己的加分项")
    @GetMapping("/extra-items")
    public Result<PageResult<ComAssessExtraItem>> myExtraItems(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name) {
        return Result.success(studentService.pageMyExtraItems(page, size, UserContext.getUserId(), status, name));
    }

    // ===== 综测成绩 =====

    @Operation(summary = "计算综测成绩")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 10, windowSeconds = 60)
    @PostMapping("/scores")
    public Result<ComAssessScore> computeScore(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return Result.success(studentService.computeScore(UserContext.getUserId(), year, semester));
    }

    @Operation(summary = "查询自己的综测成绩")
    @GetMapping("/scores")
    public Result<PageResult<ComAssessScore>> myScores(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return Result.success(studentService.pageMyScores(page, size, UserContext.getUserId(), status, year, semester));
    }

    @Operation(summary = "删除审核不通过的综测成绩")
    @DeleteMapping("/scores/{id}")
    public Result<Void> deleteScore(@PathVariable Long id) {
        studentService.deleteScore(id, UserContext.getUserId());
        return Result.success();
    }

    // ===== 违规记录 / 通知 =====

    @Operation(summary = "查询自己的违规记录（原因模糊、处分精确）")
    @GetMapping("/penalties")
    public Result<PageResult<PenaltyRecord>> myPenalties(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String punishment) {
        return Result.success(studentService.pageMyPenalties(page, size, UserContext.getUserId(), reason, punishment));
    }

    @Operation(summary = "查询自己的通知")
    @GetMapping("/notifications")
    public Result<PageResult<MessageNotification>> myNotifications(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(studentService.pageMyNotifications(page, size, UserContext.getUserId()));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notifications/{id}/read")
    public Result<Void> readNotification(@PathVariable Long id) {
        studentService.markNotificationRead(id, UserContext.getUserId());
        return Result.success();
    }
}
