package com.zongce.comprehensive.constant;

/**
 * 系统常量与枚举值定义
 * <p>数据库中相关字段均为 TINYINT，此处统一维护数字含义，避免魔法值散落各处。</p>
 */
public final class Constants {

    private Constants() {
    }

    /** 用户类型 */
    public static final String TYPE_STUDENT = "student";   // 学生
    public static final String TYPE_EMPLOYEE = "employee"; // 员工

    /** 性别 */
    public static final int SEX_MALE = 1;    // 男
    public static final int SEX_FEMALE = 2;  // 女

    /** 学生状态 */
    public static final int STUDENT_STATUS_IN = 1;          // 在读
    public static final int STUDENT_STATUS_GRADUATE = 2;    // 毕业
    public static final int STUDENT_STATUS_EXPELLED = 3;    // 开除
    public static final int STUDENT_STATUS_SUSPENDED = 4;   // 休学

    /** 学生角色 */
    public static final int STUDENT_ROLE_NORMAL = 1;          // 普通学生
    public static final int STUDENT_ROLE_STUDENT_UNION = 2;   // 学生会成员
    public static final int STUDENT_ROLE_CLUB = 3;            // 社团成员
    public static final int STUDENT_ROLE_FOREIGN = 4;         // 外国留学生

    /** 员工状态 */
    public static final int EMPLOYEE_STATUS_ON = 1;    // 在职
    public static final int EMPLOYEE_STATUS_OFF = 2;   // 离职
    public static final int EMPLOYEE_STATUS_STOP = 3;  // 停用

    /** 员工角色 */
    public static final int EMPLOYEE_ROLE_TEACHER = 1;       // 教师
    public static final int EMPLOYEE_ROLE_LEADER = 2;        // 领导
    public static final int EMPLOYEE_ROLE_COUNSELOR = 3;     // 辅导员
    public static final int EMPLOYEE_ROLE_ADMIN = 4;         // 管理员

    /** 班级状态 */
    public static final int CLAZZ_STATUS_IN = 1;          // 在读
    public static final int CLAZZ_STATUS_CANCELED = 2;    // 撤班
    public static final int CLAZZ_STATUS_GRADUATE = 3;    // 已毕业

    /** 学院状态 */
    public static final int SCHOOL_STATUS_NORMAL = 1;     // 正常
    public static final int SCHOOL_STATUS_DISSOLVED = 2;  // 解散
    public static final int SCHOOL_STATUS_DISABLED = 3;   // 未启用

    /** 活动状态 */
    public static final int ACTIVITY_STATUS_UNAUDITED = 1;    // 未审核
    public static final int ACTIVITY_STATUS_APPROVED = 2;     // 审核通过
    public static final int ACTIVITY_STATUS_REJECTED = 3;     // 审核不通过
    public static final int ACTIVITY_STATUS_CANCELED = 4;     // 已取消
    public static final int ACTIVITY_STATUS_HOLDING = 5;      // 举办中
    public static final int ACTIVITY_STATUS_FINISHED = 6;     // 已结束

    /** 活动类型 */
    public static final int ACTIVITY_TYPE_SCHOOL_THOUGHT = 1;  // 校级思想
    public static final int ACTIVITY_TYPE_SCHOOL_SPORT = 2;    // 校级文体
    public static final int ACTIVITY_TYPE_COLLEGE_THOUGHT = 3; // 院级思想
    public static final int ACTIVITY_TYPE_COLLEGE_SPORT = 4;   // 院级文体

    /** 审核状态（加分项 / 综测成绩共用） */
    public static final int AUDIT_STATUS_UNAUDITED = 1;  // 未审核
    public static final int AUDIT_STATUS_APPROVED = 2;   // 审核通过
    public static final int AUDIT_STATUS_REJECTED = 3;   // 审核不通过

    /** 通知接收人类型 */
    public static final int RECEIVER_STUDENT = 1;   // 学生
    public static final int RECEIVER_EMPLOYEE = 2;  // 员工

    /** 综测计算公式分值 */
    public static final double SCORE_SCHOOL_ACTIVITY = 0.2;   // 校级活动 0.2 分
    public static final double SCORE_COLLEGE_ACTIVITY = 0.1;  // 院级活动 0.1 分

    /** 综测一年最多审核通过次数 */
    public static final int SCORE_MAX_APPROVED_PER_YEAR = 2;
}
