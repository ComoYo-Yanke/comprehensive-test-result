package com.zongce.comprehensive.config;

import com.zongce.comprehensive.common.PasswordUtil;
import com.zongce.comprehensive.constant.Constants;
import com.zongce.comprehensive.entity.Clazz;
import com.zongce.comprehensive.entity.ComAssessActivity;
import com.zongce.comprehensive.entity.Employee;
import com.zongce.comprehensive.entity.EmployeeSchool;
import com.zongce.comprehensive.entity.Major;
import com.zongce.comprehensive.entity.School;
import com.zongce.comprehensive.entity.Student;
import com.zongce.comprehensive.entity.StudentMajor;
import com.zongce.comprehensive.mapper.ClazzMapper;
import com.zongce.comprehensive.mapper.ComAssessActivityMapper;
import com.zongce.comprehensive.mapper.EmployeeMapper;
import com.zongce.comprehensive.mapper.EmployeeSchoolMapper;
import com.zongce.comprehensive.mapper.MajorMapper;
import com.zongce.comprehensive.mapper.SchoolMapper;
import com.zongce.comprehensive.mapper.StudentMajorMapper;
import com.zongce.comprehensive.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 演示数据初始化
 * <p>首次启动（学院表为空）时自动写入学院、专业、班级、默认账号与示例活动，
 * 便于项目 clone 后即可登录演示。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SchoolMapper schoolMapper;
    private final MajorMapper majorMapper;
    private final ClazzMapper clazzMapper;
    private final EmployeeMapper employeeMapper;
    private final EmployeeSchoolMapper employeeSchoolMapper;
    private final StudentMapper studentMapper;
    private final StudentMajorMapper studentMajorMapper;
    private final ComAssessActivityMapper activityMapper;

    @Override
    public void run(String... args) {
        if (schoolMapper.selectCount(null) > 0) {
            return; // 已初始化
        }
        init();
        log.info("演示数据初始化完成");
    }

    private void init() {
        // 学院
        School cs = saveSchool("计算机学院");
        School fl = saveSchool("外国语学院");

        // 专业
        Major se = saveMajor("软件工程", cs.getId());
        Major csMajor = saveMajor("计算机科学与技术", cs.getId());
        Major en = saveMajor("英语", fl.getId());

        // 班级
        Clazz c1 = saveClazz("软件2201", cs.getId(), se.getId());
        Clazz c2 = saveClazz("计科2201", cs.getId(), csMajor.getId());
        Clazz c3 = saveClazz("英语2201", fl.getId(), en.getId());

        // 员工：管理员
        Employee admin = new Employee();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.encode("admin123"));
        admin.setName("系统管理员");
        admin.setRole(Constants.EMPLOYEE_ROLE_ADMIN);
        admin.setStatus(Constants.EMPLOYEE_STATUS_ON);
        admin.setNumber("110101198001010011");
        admin.setSex(Constants.SEX_MALE);
        employeeMapper.insert(admin);

        // 员工：辅导员（管理软件2201）
        Employee counselor = new Employee();
        counselor.setUsername("2001");
        counselor.setPassword(PasswordUtil.encode("123456"));
        counselor.setName("王辅导员");
        counselor.setRole(Constants.EMPLOYEE_ROLE_COUNSELOR);
        counselor.setStatus(Constants.EMPLOYEE_STATUS_ON);
        counselor.setClazzId(c1.getId());
        counselor.setNumber("110101198501010021");
        counselor.setSex(Constants.SEX_MALE);
        employeeMapper.insert(counselor);

        // 员工：教师（任职计算机学院）
        Employee teacher = new Employee();
        teacher.setUsername("2002");
        teacher.setPassword(PasswordUtil.encode("123456"));
        teacher.setName("李老师");
        teacher.setRole(Constants.EMPLOYEE_ROLE_TEACHER);
        teacher.setStatus(Constants.EMPLOYEE_STATUS_ON);
        teacher.setNumber("110101198601010031");
        teacher.setSex(Constants.SEX_FEMALE);
        employeeMapper.insert(teacher);
        saveEmployeeSchool(teacher.getId(), cs.getId());

        // 学生（默认密码 = 身份证后6位）
        saveStudent("20220101", "张三", Constants.STUDENT_ROLE_NORMAL, c1.getId(), cs.getId(), se.getId(),
                "110101200401011234");
        saveStudent("20220102", "李四", Constants.STUDENT_ROLE_STUDENT_UNION, c1.getId(), cs.getId(), se.getId(),
                "110101200401011235");
        saveStudent("20220103", "王五", Constants.STUDENT_ROLE_NORMAL, c3.getId(), fl.getId(), en.getId(),
                "110101200401011236");

        // 示例活动（已通过，校级文体）
        ComAssessActivity act1 = new ComAssessActivity();
        act1.setName("春季校运会");
        act1.setType(Constants.ACTIVITY_TYPE_SCHOOL_SPORT);
        act1.setStatus(Constants.ACTIVITY_STATUS_APPROVED);
        act1.setLimitNum(50);
        act1.setStartTime(LocalDateTime.now().minusDays(1));
        act1.setEndTime(LocalDateTime.now().plusDays(30));
        act1.setDescription("全校春季运动会，报名即可参加。");
        activityMapper.insert(act1);

        // 示例活动（已通过，院级思想）
        ComAssessActivity act2 = new ComAssessActivity();
        act2.setName("计算机学院学术讲座");
        act2.setType(Constants.ACTIVITY_TYPE_COLLEGE_THOUGHT);
        act2.setStatus(Constants.ACTIVITY_STATUS_APPROVED);
        act2.setSchoolId(cs.getId());
        act2.setLimitNum(100);
        act2.setStartTime(LocalDateTime.now().minusDays(2));
        act2.setEndTime(LocalDateTime.now().plusDays(15));
        act2.setDescription("邀请专家开展学术讲座。");
        activityMapper.insert(act2);
    }

    private School saveSchool(String name) {
        School school = new School();
        school.setName(name);
        school.setStatus(Constants.SCHOOL_STATUS_NORMAL);
        schoolMapper.insert(school);
        return school;
    }

    private Major saveMajor(String name, Long schoolId) {
        Major major = new Major();
        major.setName(name);
        major.setSchoolId(schoolId);
        majorMapper.insert(major);
        return major;
    }

    private Clazz saveClazz(String name, Long schoolId, Long majorId) {
        Clazz clazz = new Clazz();
        clazz.setName(name);
        clazz.setSchoolId(schoolId);
        clazz.setMajorId(majorId);
        clazz.setStatus(Constants.CLAZZ_STATUS_IN);
        clazzMapper.insert(clazz);
        return clazz;
    }

    private void saveEmployeeSchool(Long employeeId, Long schoolId) {
        EmployeeSchool es = new EmployeeSchool();
        es.setEmployeeId(employeeId);
        es.setSchoolId(schoolId);
        employeeSchoolMapper.insert(es);
    }

    private void saveStudent(String username, String name, int role, Long clazzId, Long schoolId,
                             Long majorId, String number) {
        Student student = new Student();
        student.setUsername(username);
        student.setName(name);
        student.setRole(role);
        student.setStatus(Constants.STUDENT_STATUS_IN);
        student.setClazzId(clazzId);
        student.setSchoolId(schoolId);
        student.setNumber(number);
        student.setSex(Constants.SEX_MALE);
        student.setEnrollTime("2022-09");
        student.setEmail(username + "@stu.edu.cn");
        student.setPassword(PasswordUtil.encode(PasswordUtil.defaultPassword(number)));
        studentMapper.insert(student);

        StudentMajor sm = new StudentMajor();
        sm.setStudentId(student.getId());
        sm.setMajorId(majorId);
        studentMajorMapper.insert(sm);
    }
}
