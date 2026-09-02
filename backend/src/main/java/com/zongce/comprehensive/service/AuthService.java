package com.zongce.comprehensive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.common.CurrentUser;
import com.zongce.comprehensive.common.JwtUtil;
import com.zongce.comprehensive.common.PasswordUtil;
import com.zongce.comprehensive.common.UserContext;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.dto.LoginDTO;
import com.zongce.comprehensive.dto.RegisterDTO;
import com.zongce.comprehensive.entity.Employee;
import com.zongce.comprehensive.entity.Student;
import com.zongce.comprehensive.mapper.EmployeeMapper;
import com.zongce.comprehensive.mapper.StudentMapper;
import com.zongce.comprehensive.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 认证服务：登录（含验证码校验与失败锁定）、注册、获取当前用户
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 连续输错密码达到该次数即锁定账号 */
    private static final int MAX_FAIL = 5;
    /** 失败计数统计窗口时长 */
    private static final Duration FAIL_TTL = Duration.ofMinutes(10);
    /** 账号锁定时长 */
    private static final Duration LOCK_TTL = Duration.ofMinutes(15);

    private final StudentMapper studentMapper;
    private final EmployeeMapper employeeMapper;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    private final StringRedisTemplate stringRedisTemplate;

    /** 学生登录 */
    public LoginVO studentLogin(LoginDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUsername, dto.getUsername()));
        // 账号不存在时不记录失败次数，避免被恶意锁定任意账号
        if (student == null) {
            throw new BusinessException("账号或密码错误");
        }
        checkLocked("student", dto.getUsername());
        if (!PasswordUtil.matches(dto.getPassword(), student.getPassword())) {
            recordFail("student", dto.getUsername());
            throw new BusinessException("账号或密码错误");
        }
        clearFail("student", dto.getUsername());
        if (student.getStatus() != null && student.getStatus() != Constants.STUDENT_STATUS_IN) {
            throw new BusinessException("账号状态异常，无法登录");
        }
        return buildLoginVO(new CurrentUser(student.getId(), Constants.TYPE_STUDENT,
                student.getRole(), student.getUsername()), student.getName());
    }

    /** 员工登录 */
    public LoginVO employeeLogin(LoginDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        Employee employee = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, dto.getUsername()));
        if (employee == null) {
            throw new BusinessException("账号或密码错误");
        }
        checkLocked("employee", dto.getUsername());
        if (!PasswordUtil.matches(dto.getPassword(), employee.getPassword())) {
            recordFail("employee", dto.getUsername());
            throw new BusinessException("账号或密码错误");
        }
        clearFail("employee", dto.getUsername());
        if (employee.getStatus() != null && employee.getStatus() == Constants.EMPLOYEE_STATUS_STOP) {
            throw new BusinessException("账号已停用，无法登录");
        }
        return buildLoginVO(new CurrentUser(employee.getId(), Constants.TYPE_EMPLOYEE,
                employee.getRole(), employee.getUsername()), employee.getName());
    }

    /** 员工注册 */
    public void employeeRegister(RegisterDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("该工号已注册");
        }
        Employee employee = new Employee();
        employee.setUsername(dto.getUsername());
        employee.setPassword(PasswordUtil.encode(dto.getPassword()));
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setNumber(dto.getNumber());
        employee.setSex(dto.getSex());
        employee.setRole(dto.getRole() == null ? Constants.EMPLOYEE_ROLE_TEACHER : dto.getRole());
        employee.setStatus(Constants.EMPLOYEE_STATUS_ON);
        employee.setAddress(dto.getAddress());
        employee.setEmail(dto.getEmail());
        employee.setDescription(dto.getDescription());
        employeeMapper.insert(employee);
    }

    /** 获取当前登录用户信息（学生或员工实体，密码已脱敏） */
    public Object currentUser() {
        CurrentUser user = UserContext.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录");
        }
        if (Constants.TYPE_STUDENT.equals(user.getUserType())) {
            Student student = studentMapper.selectById(user.getUserId());
            student.setPassword(null);
            return student;
        } else {
            Employee employee = employeeMapper.selectById(user.getUserId());
            employee.setPassword(null);
            return employee;
        }
    }

    // ============ 内部工具 ============

    /** 校验图形验证码（一次性，校验即失效） */
    private void verifyCaptcha(String captchaId, String captchaCode) {
        if (!captchaService.verify(captchaId, captchaCode)) {
            throw new BusinessException("验证码错误或已过期，请刷新后重试");
        }
    }

    private String failKey(String type, String username) {
        return "login:fail:" + type + ":" + username;
    }

    private String lockKey(String type, String username) {
        return "login:lock:" + type + ":" + username;
    }

    /** 检查账号是否已被临时锁定 */
    private void checkLocked(String type, String username) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey(type, username)))) {
            throw new BusinessException("登录失败次数过多，账号已临时锁定，请15分钟后再试");
        }
    }

    /** 记录一次密码错误，连续达到阈值则锁定账号 */
    private void recordFail(String type, String username) {
        String key = failKey(type, username);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, FAIL_TTL);
        }
        if (count != null && count >= MAX_FAIL) {
            stringRedisTemplate.opsForValue().set(lockKey(type, username), "1", LOCK_TTL);
        }
    }

    /** 登录成功：清除失败计数与锁定状态 */
    private void clearFail(String type, String username) {
        stringRedisTemplate.delete(failKey(type, username));
        stringRedisTemplate.delete(lockKey(type, username));
    }

    /** 组装登录返回结果 */
    private LoginVO buildLoginVO(CurrentUser user, String name) {
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user));
        vo.setUserId(user.getUserId());
        vo.setUserType(user.getUserType());
        vo.setRole(user.getRole());
        vo.setUsername(user.getUsername());
        vo.setName(name);
        return vo;
    }
}
