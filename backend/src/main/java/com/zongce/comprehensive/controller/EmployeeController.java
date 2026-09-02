package com.zongce.comprehensive.controller;

import com.zongce.comprehensive.common.PageResult;
import com.zongce.comprehensive.common.RateLimit;
import com.zongce.comprehensive.common.RequireRole;
import com.zongce.comprehensive.common.Result;
import com.zongce.comprehensive.common.UserContext;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.PenaltyAddDTO;
import com.zongce.comprehensive.dto.ReviewDTO;
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
import com.zongce.comprehensive.service.EmployeeService;
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

import java.util.List;
import java.util.Map;

/**
 * 员工端（管理端）接口
 */
@Tag(name = "员工端接口")
@RestController
@RequestMapping("/api/v1/employee")
@RequireRole(Constants.TYPE_EMPLOYEE)
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ===== 学生管理 =====

    @Operation(summary = "分页查询学生")
    @GetMapping("/students")
    public Result<PageResult<Student>> students(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {
        return Result.success(employeeService.pageStudents(page, size, name, username, phone,
                clazzId, schoolId, status, role));
    }

    @Operation(summary = "学生详情")
    @GetMapping("/students/{id}")
    public Result<Student> student(@PathVariable Long id) {
        return Result.success(employeeService.getStudent(id));
    }

    @Operation(summary = "新增学生")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/students")
    public Result<Void> addStudent(@RequestBody Student student,
                                   @RequestParam(required = false) List<Long> majorIds) {
        employeeService.addStudent(student, majorIds);
        return Result.success();
    }

    @Operation(summary = "修改学生")
    @PutMapping("/students")
    public Result<Void> updateStudent(@RequestBody Student student,
                                      @RequestParam(required = false) List<Long> majorIds) {
        employeeService.updateStudent(student, majorIds);
        return Result.success();
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/students/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        employeeService.deleteStudent(id);
        return Result.success();
    }

    @Operation(summary = "重置学生密码")
    @PutMapping("/students/{id}/reset-password")
    public Result<Void> resetStudentPassword(@PathVariable Long id) {
        employeeService.resetStudentPassword(id);
        return Result.success();
    }

    // ===== 员工管理 =====

    @Operation(summary = "分页查询员工")
    @GetMapping("/employees")
    public Result<PageResult<Employee>> employees(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        return Result.success(employeeService.pageEmployees(page, size, name, username, phone, role, status));
    }

    @Operation(summary = "员工详情")
    @GetMapping("/employees/{id}")
    public Result<Employee> employee(@PathVariable Long id) {
        return Result.success(employeeService.getEmployee(id));
    }

    @Operation(summary = "新增员工")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/employees")
    public Result<Void> addEmployee(@RequestBody Employee employee,
                                    @RequestParam(required = false) List<Long> schoolIds) {
        employeeService.addEmployee(employee, schoolIds);
        return Result.success();
    }

    @Operation(summary = "修改员工")
    @PutMapping("/employees")
    public Result<Void> updateEmployee(@RequestBody Employee employee,
                                       @RequestParam(required = false) List<Long> schoolIds) {
        employeeService.updateEmployee(employee, schoolIds);
        return Result.success();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/employees/{id}")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    // ===== 学院管理 =====

    @Operation(summary = "分页查询学院")
    @GetMapping("/schools")
    public Result<PageResult<School>> schools(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.success(employeeService.pageSchools(page, size, name, status));
    }

    @Operation(summary = "新增学院")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/schools")
    public Result<Void> addSchool(@RequestBody School school) {
        employeeService.addSchool(school);
        return Result.success();
    }

    @Operation(summary = "修改学院")
    @PutMapping("/schools")
    public Result<Void> updateSchool(@RequestBody School school) {
        employeeService.updateSchool(school);
        return Result.success();
    }

    @Operation(summary = "删除学院")
    @DeleteMapping("/schools/{id}")
    public Result<Void> deleteSchool(@PathVariable Long id) {
        employeeService.deleteSchool(id);
        return Result.success();
    }

    // ===== 专业管理 =====

    @Operation(summary = "分页查询专业")
    @GetMapping("/majors")
    public Result<PageResult<Major>> majors(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId) {
        return Result.success(employeeService.pageMajors(page, size, name, schoolId));
    }

    @Operation(summary = "新增专业")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/majors")
    public Result<Void> addMajor(@RequestBody Major major) {
        employeeService.addMajor(major);
        return Result.success();
    }

    @Operation(summary = "修改专业")
    @PutMapping("/majors")
    public Result<Void> updateMajor(@RequestBody Major major) {
        employeeService.updateMajor(major);
        return Result.success();
    }

    @Operation(summary = "删除专业")
    @DeleteMapping("/majors/{id}")
    public Result<Void> deleteMajor(@PathVariable Long id) {
        employeeService.deleteMajor(id);
        return Result.success();
    }

    // ===== 班级管理 =====

    @Operation(summary = "分页查询班级")
    @GetMapping("/clazzs")
    public Result<PageResult<Clazz>> clazzs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer status) {
        return Result.success(employeeService.pageClazzs(page, size, name, schoolId, majorId, status));
    }

    @Operation(summary = "新增班级")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/clazzs")
    public Result<Void> addClazz(@RequestBody Clazz clazz) {
        employeeService.addClazz(clazz);
        return Result.success();
    }

    @Operation(summary = "修改班级")
    @PutMapping("/clazzs")
    public Result<Void> updateClazz(@RequestBody Clazz clazz) {
        employeeService.updateClazz(clazz);
        return Result.success();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/clazzs/{id}")
    public Result<Void> deleteClazz(@PathVariable Long id) {
        employeeService.deleteClazz(id);
        return Result.success();
    }

    // ===== 活动管理 =====

    @Operation(summary = "分页查询活动")
    @GetMapping("/activities")
    public Result<PageResult<ComAssessActivity>> activities(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long schoolId) {
        return Result.success(employeeService.pageActivities(page, size, name, type, status, schoolId));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/activities/{id}")
    public Result<ComAssessActivity> activity(@PathVariable Long id) {
        return Result.success(employeeService.getActivity(id));
    }

    @Operation(summary = "修改活动")
    @PutMapping("/activities")
    public Result<Void> updateActivity(@RequestBody ComAssessActivity activity) {
        employeeService.updateActivity(activity);
        return Result.success();
    }

    @Operation(summary = "审核活动")
    @PutMapping("/activities/{id}/review")
    public Result<Void> reviewActivity(@PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        employeeService.reviewActivity(id, dto, UserContext.getUserId());
        return Result.success();
    }

    // ===== 加分项审核 =====

    @Operation(summary = "分页查询加分项")
    @GetMapping("/extra-items")
    public Result<PageResult<ComAssessExtraItem>> extraItems(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String reason) {
        return Result.success(employeeService.pageExtraItems(page, size, status, studentId, name, reason));
    }

    @Operation(summary = "审核加分项")
    @PutMapping("/extra-items/{id}/review")
    public Result<Void> reviewExtraItem(@PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        employeeService.reviewExtraItem(id, dto, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "分页查询本班加分项（辅导员）")
    @GetMapping("/my-class/extra-items")
    public Result<PageResult<ComAssessExtraItem>> myClassExtraItems(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String reason) {
        return Result.success(employeeService.pageMyClassExtraItems(page, size, status, name, reason,
                UserContext.getUserId()));
    }

    @Operation(summary = "审核本班加分项（辅导员）")
    @PutMapping("/my-class/extra-items/{id}/review")
    public Result<Void> reviewMyClassExtraItem(@PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        employeeService.reviewMyClassExtraItem(id, dto, UserContext.getUserId());
        return Result.success();
    }

    // ===== 违规记录 =====

    @Operation(summary = "添加违规记录")
    @RateLimit(scope = RateLimit.Scope.USER, key = "write", limit = 20, windowSeconds = 60)
    @PostMapping("/penalties")
    public Result<Void> addPenalty(@Valid @RequestBody PenaltyAddDTO dto) {
        employeeService.addPenalty(dto);
        return Result.success();
    }

    @Operation(summary = "分页查询违规记录")
    @GetMapping("/penalties")
    public Result<PageResult<PenaltyRecord>> penalties(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String reason) {
        return Result.success(employeeService.pagePenalties(page, size, studentId, name, reason));
    }

    // ===== 综测成绩 =====

    @Operation(summary = "查询所有已审核通过的综测成绩")
    @GetMapping("/scores/approved")
    public Result<PageResult<ComAssessScore>> approvedScores(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return Result.success(employeeService.pageApprovedScores(page, size, studentId, year, semester));
    }

    @Operation(summary = "查询待审核综测成绩")
    @GetMapping("/scores/pending")
    public Result<PageResult<ComAssessScore>> pendingScores(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return Result.success(employeeService.pagePendingScores(page, size, studentId, year, semester));
    }

    @Operation(summary = "审核综测成绩")
    @PutMapping("/scores/{id}/review")
    public Result<Void> reviewScore(@PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        employeeService.reviewScore(id, dto, UserContext.getUserId());
        return Result.success();
    }

    // ===== 下拉选项（新增/编辑表单回显用） =====

    @Operation(summary = "学院下拉选项")
    @GetMapping("/options/schools")
    public Result<List<School>> schoolOptions() {
        return Result.success(employeeService.listSchoolOptions());
    }

    @Operation(summary = "专业下拉选项（可按学院过滤）")
    @GetMapping("/options/majors")
    public Result<List<Major>> majorOptions(@RequestParam(required = false) Long schoolId) {
        return Result.success(employeeService.listMajorOptions(schoolId));
    }

    @Operation(summary = "班级下拉选项（可按学院/专业过滤）")
    @GetMapping("/options/clazzs")
    public Result<List<Clazz>> clazzOptions(@RequestParam(required = false) Long schoolId,
                                            @RequestParam(required = false) Long majorId) {
        return Result.success(employeeService.listClazzOptions(schoolId, majorId));
    }

    // ===== 统计 =====

    @Operation(summary = "查看统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(employeeService.statistics());
    }

    // ===== 通知 =====

    @Operation(summary = "分页查询通知")
    @GetMapping("/notifications")
    public Result<PageResult<MessageNotification>> notifications(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(employeeService.pageNotifications(page, size, UserContext.getUserId()));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notifications/{id}/read")
    public Result<Void> readNotification(@PathVariable Long id) {
        employeeService.markNotificationRead(id, UserContext.getUserId());
        return Result.success();
    }
}
