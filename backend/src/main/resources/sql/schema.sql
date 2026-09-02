-- =====================================================
-- 学生综测统计系统 数据库初始化脚本
-- 数据库名：comprehensive
-- =====================================================
CREATE DATABASE IF NOT EXISTS comprehensive DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE comprehensive;

-- 员工表
CREATE TABLE IF NOT EXISTS employee (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(32)  NOT NULL COMMENT '工号（登录账号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    number      VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    sex         TINYINT      DEFAULT NULL COMMENT '性别 1男 2女',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1在职 2离职 3停用',
    role        TINYINT      DEFAULT 1 COMMENT '角色 1教师 2领导 3辅导员 4管理员',
    clazz_id    BIGINT       DEFAULT NULL COMMENT '辅导员管理班级id',
    address     VARCHAR(255) DEFAULT NULL COMMENT '地址',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(32)  NOT NULL COMMENT '学号（登录账号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '电话',
    number      VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    sex         TINYINT      DEFAULT NULL COMMENT '性别 1男 2女',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1在读 2毕业 3开除 4休学',
    role        TINYINT      DEFAULT 1 COMMENT '角色 1普通学生 2学生会 3社团 4外国留学生',
    clazz_id    BIGINT       DEFAULT NULL COMMENT '班级id',
    school_id   BIGINT       DEFAULT NULL COMMENT '学院id',
    enroll_time VARCHAR(7)   DEFAULT NULL COMMENT '入学年月 YYYY-MM',
    address     VARCHAR(255) DEFAULT NULL COMMENT '地址',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_clazz (clazz_id),
    KEY idx_school (school_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生表';

-- 班级表
CREATE TABLE IF NOT EXISTS clazz (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '班级名称',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1在读 2撤班 3已毕业',
    school_id   BIGINT       DEFAULT NULL COMMENT '学院id',
    major_id    BIGINT       DEFAULT NULL COMMENT '专业id',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_school (school_id),
    KEY idx_major (major_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '班级表';

-- 学院表
CREATE TABLE IF NOT EXISTS school (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '学院名称',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1正常 2解散 3未启用',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学院表';

-- 专业表
CREATE TABLE IF NOT EXISTS major (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '专业名称',
    school_id   BIGINT       DEFAULT NULL COMMENT '学院id',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_school (school_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '专业表';

-- 综测活动表
CREATE TABLE IF NOT EXISTS com_assess_activity (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) DEFAULT NULL COMMENT '活动名称',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1未审核 2审核通过 3审核不通过 4已取消 5举办中 6已结束',
    school_id   BIGINT       DEFAULT NULL COMMENT '创办学院id（校级为null）',
    type        TINYINT      DEFAULT NULL COMMENT '类型 1校级思想 2校级文体 3院级思想 4院级文体',
    limit_num   INT          DEFAULT NULL COMMENT '限制人数',
    start_time  DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time    DATETIME     DEFAULT NULL COMMENT '结束时间',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人（学生）',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_school (school_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '综测活动表';

-- 其他综测加分事项表
CREATE TABLE IF NOT EXISTS com_assess_extra_item (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100)  DEFAULT NULL COMMENT '加分项名称',
    student_id  BIGINT        DEFAULT NULL COMMENT '学生id',
    evidence    VARCHAR(255)  DEFAULT NULL COMMENT '佐证图片URL',
    score       DECIMAL(6, 2) DEFAULT NULL COMMENT '加分分数',
    status      TINYINT       DEFAULT 1 COMMENT '审核状态 1未审核 2通过 3不通过',
    reason      VARCHAR(500)  DEFAULT NULL COMMENT '不通过原因',
    reviewer_id BIGINT        DEFAULT NULL COMMENT '审核员工id',
    description VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT        DEFAULT NULL COMMENT '创建人',
    update_user BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_student (student_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '其他综测加分事项表';

-- 综测成绩表
CREATE TABLE IF NOT EXISTS com_assess_score (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id    BIGINT        DEFAULT NULL COMMENT '学生id',
    year          INT           DEFAULT NULL COMMENT '年份',
    semester      TINYINT       DEFAULT NULL COMMENT '学期 1春季 2秋季',
    activity_score DECIMAL(6, 2) DEFAULT 0 COMMENT '活动分',
    extra_score   DECIMAL(6, 2) DEFAULT 0 COMMENT '其他加分',
    penalty_score DECIMAL(6, 2) DEFAULT 0 COMMENT '违规扣分',
    score         DECIMAL(6, 2) DEFAULT 0 COMMENT '总成绩',
    status        TINYINT       DEFAULT 1 COMMENT '审核状态 1未审核 2通过 3不通过',
    reason        VARCHAR(500)  DEFAULT NULL COMMENT '不通过原因',
    reviewer_id   BIGINT        DEFAULT NULL COMMENT '审核员工id',
    description   VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user   BIGINT        DEFAULT NULL COMMENT '创建人',
    update_user   BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_student (student_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '综测成绩表';

-- 违规记录表
CREATE TABLE IF NOT EXISTS penalty_record (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT        DEFAULT NULL COMMENT '学生id',
    name        VARCHAR(100)  DEFAULT NULL COMMENT '违规名称',
    reason      VARCHAR(500)  DEFAULT NULL COMMENT '违规原因',
    score       DECIMAL(6, 2) DEFAULT NULL COMMENT '扣分分数',
    punishment  VARCHAR(100)  DEFAULT NULL COMMENT '处分内容',
    description VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT        DEFAULT NULL COMMENT '创建人',
    update_user BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_student (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '违规记录表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS message_notification (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    receiver_id   BIGINT        DEFAULT NULL COMMENT '接收人id',
    receiver_type TINYINT       DEFAULT NULL COMMENT '接收人类型 1学生 2员工',
    title         VARCHAR(100)  DEFAULT NULL COMMENT '标题',
    content       VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    type          TINYINT       DEFAULT NULL COMMENT '通知类型',
    is_read       TINYINT       DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    related_id    BIGINT        DEFAULT NULL COMMENT '关联业务id',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user   BIGINT        DEFAULT NULL COMMENT '创建人',
    update_user   BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_receiver (receiver_id, receiver_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息通知表';

-- 员工-学院 关联表
CREATE TABLE IF NOT EXISTS employee_school (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    employee_id BIGINT NOT NULL COMMENT '员工id',
    school_id   BIGINT NOT NULL COMMENT '学院id',
    PRIMARY KEY (id),
    KEY idx_employee (employee_id),
    KEY idx_school (school_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工-学院关联表';

-- 学生-专业 关联表
CREATE TABLE IF NOT EXISTS student_major (
    id         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生id',
    major_id   BIGINT NOT NULL COMMENT '专业id',
    PRIMARY KEY (id),
    KEY idx_student (student_id),
    KEY idx_major (major_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生-专业关联表';

-- 活动-负责老师 关联表
CREATE TABLE IF NOT EXISTS activity_employee (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    activity_id BIGINT NOT NULL COMMENT '活动id',
    employee_id BIGINT NOT NULL COMMENT '员工id',
    PRIMARY KEY (id),
    KEY idx_activity (activity_id),
    KEY idx_employee (employee_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动-负责老师关联表';

-- 活动-负责学生 关联表
CREATE TABLE IF NOT EXISTS activity_charge_student (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    activity_id BIGINT NOT NULL COMMENT '活动id',
    student_id  BIGINT NOT NULL COMMENT '学生id',
    PRIMARY KEY (id),
    KEY idx_activity (activity_id),
    KEY idx_student (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动-负责学生关联表';

-- 活动-参加学生 关联表（join_time 毫秒精度）
CREATE TABLE IF NOT EXISTS activity_student (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    activity_id BIGINT      NOT NULL COMMENT '活动id',
    student_id  BIGINT      NOT NULL COMMENT '学生id',
    join_time   DATETIME(3) DEFAULT NULL COMMENT '加入时间（毫秒）',
    PRIMARY KEY (id),
    KEY idx_activity (activity_id),
    KEY idx_student (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '活动-参加学生关联表';
