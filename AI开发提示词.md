# 学生综测统计系统 —— AI 开发提示词（优化版）

> 把下面整段内容一次性发给 AI，让它直接生成**完整、可运行、可直接编译启动**的项目。
> 不要让它输出"提示词总结"或"代码片段示例"，要求它直接写项目文件和完整代码。

---

## 一、角色设定

你是一名资深 Java 全栈工程师，精通 Spring Boot 3、Vue 3、MySQL、Redis。请严格按照下述需求，从零开始生成一个**完整、可运行、可直接编译启动**的"学生综测统计系统"。

**硬性要求：**
- 直接生成项目文件与完整代码，不要输出提示词总结、不要只给代码片段、不要用省略号/占位符代替核心逻辑。
- 按正常工程目录结构组织，保证 clone 下来能跑。
- 所有代码注释、字段注释、接口文档、README 一律使用**中文**。
- 控制台输出保持精简，只打印关键进度信息。

---

## 二、项目概述

项目名称：**学生综测统计系统**（高校学生综合素质测评系统）。

分**两个使用端**，前端做物理区分，后端不做物理区分（仅通过角色做权限拦截）：
1. **管理端**：员工（教师 / 辅导员 / 领导 / 管理员）。
2. **用户端**：学生。

两端都需登录与鉴权。

---

## 三、技术栈与版本（严格锁定，不要擅自更换）

| 类别 | 技术 | 版本/说明 |
| --- | --- | --- |
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.3.x |
| 构建 | Maven | 3.8+ |
| ORM | MyBatis-Plus | 3.5.7（`mybatis-plus-spring-boot3-starter`） |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis + Spring Cache | 注解 + Redis 实现 |
| 鉴权 | JWT | `io.jsonwebtoken:jjwt` 0.12.6 |
| 加密 | BCrypt | spring-security-crypto 或 hutool |
| 实时通信 | WebSocket | spring-boot-starter-websocket + Redis 发布订阅实现集群广播 |
| 定时任务 | Spring Task | `@Scheduled` |
| 接口文档 | Knife4j | 4.4.0（openapi3 jakarta） |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts | 最新稳定版 |
| 部署 | Nginx | 前后端分离、反向代理 |

---

## 四、数据库配置

根目录 `datasource.config` 内容如下（YAML，缩进可能是 Tab，需容错解析），含 mysql 和 redis 两段，程序启动时读取该文件，**不得硬编码密码**：

```yaml
mysql:
    host: localhost
    port: 3306
    username: root
    password: xxxxxx
redis:
    host: localhost
    port: 6379
    password: xxxxxx
    database: 0
```

- 数据库名：`comprehensive`（脚本里 `CREATE DATABASE IF NOT EXISTS`）。
- 连接串需加 `useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false`。

---

## 五、系统架构约定

- 前后端分离；后端统一 API 前缀 `/api/v1/`。
- 统一响应格式 `{code, msg, data}`：`code=1` 成功、`code=0` 失败。
- 分层 `Controller → Service → Mapper → Entity`。
- 全局异常拦截 `@RestControllerAdvice` + JWT 拦截器。
- 密码 BCrypt 加密；**默认密码**：身份证号后 6 位，无身份证用当前时间数字；**忘记密码只能由管理员重置**。

---

## 六、目录结构（必须按此组织）

```
项目根目录/
├── datasource.config
├── backend/                    # Spring Boot 后端（groupId=com.zongce, artifactId=comprehensive, 包 com.zongce.comprehensive）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/zongce/comprehensive/
│       │   ├── ComprehensiveApplication.java
│       │   ├── config/         # 数据源、Redis、缓存、WebMvc、WebSocket、MyBatisPlus、Knife4j、拦截器
│       │   ├── constant/       # 枚举、常量
│       │   ├── common/         # Result、异常、JwtUtil、分页、UserContext
│       │   ├── entity/         # 实体
│       │   ├── mapper/         # MyBatis-Plus Mapper
│       │   ├── dto/            # 入参
│       │   ├── vo/             # 出参
│       │   ├── service/ + impl/
│       │   ├── controller/
│       │   ├── websocket/      # WebSocket 服务 + Redis 监听
│       │   └── task/           # 定时任务
│       └── resources/
│           ├── application.yml
│           └── sql/schema.sql + data.sql
└── frontend/
    ├── admin/                  # 管理端 Vue3 工程
    └── user/                   # 用户端 Vue3 工程
```

---

## 七、数据库表设计（10 张核心表 + 5 张中间表）

> 主键统一 `id BIGINT AUTO_INCREMENT`；金额/分数用 `DECIMAL(6,2)`；时间 `DATETIME`；状态/角色/类型用 `TINYINT`（枚举见下）。多对多关系用中间表。

### 核心表

**1. employee 员工表**
username(工号,唯一,登录用)、password、name、phone、number(身份证)、sex、status、role、clazz_id(辅导员专属班级,单值)、address、email、description、create_time、update_time、create_user、update_user

**2. student 学生表**
username(学号,唯一,登录用)、password、name、phone、number(身份证)、sex、status、role、clazz_id、school_id、enroll_time(VARCHAR(7),入学年月)、address、email、description、create_time、update_time、create_user、update_user

**3. clazz 班级表**
name、status、school_id、major_id、description、create_time、update_time、create_user、update_user

**4. school 学院表**
name、status、description、create_time、update_time、create_user、update_user

**5. major 专业表**
name、school_id、description、create_time、update_time、create_user、update_user

**6. com_assess_activity 综测活动表**
name、status、school_id(校级为 NULL)、type、limit_num(限制人数)、start_time、end_time、description、create_time、update_time、create_user、update_user

**7. com_assess_extra_item 其他综测加分项表**
name、student_id、evidence(佐证图片URL)、score、status、reason、reviewer_id、description、create_time、update_time、create_user、update_user

**8. com_assess_score 综测成绩表**
student_id、year(年份)、semester(学期)、activity_score(活动分)、extra_score(其他加分)、penalty_score(违规扣分)、score(总成绩)、status、reason、reviewer_id、description、create_time、update_time、create_user、update_user

**9. penalty_record 违规记录表**
student_id、name、reason、score(扣分,正数)、punishment(处分)、create_time、update_time、create_user、update_user

**10. message_notification 消息通知表**
receiver_id、receiver_type、title、content、type、is_read、related_id、create_time、update_time、create_user、update_user

### 中间表（多对多）

- **employee_school**(employee_id, school_id) —— 员工可任职多学院
- **student_major**(student_id, major_id) —— 学生可修多专业
- **activity_employee**(activity_id, employee_id) —— 活动负责老师（多）
- **activity_charge_student**(activity_id, student_id) —— 活动负责学生（多）
- **activity_student**(activity_id, student_id, join_time DATETIME(3)) —— 参加学生，`join_time` 毫秒级，用于"踢出最后加入者"

### 枚举定义（全部 TINYINT）

| 字段 | 取值 |
| --- | --- |
| sex | 1=男, 2=女 |
| student.status | 1=在读, 2=毕业, 3=开除, 4=休学 |
| student.role | 1=普通学生, 2=学生会成员, 3=社团成员, 4=外国留学生 |
| employee.status | 1=在职, 2=离职, 3=停用 |
| employee.role | 1=教师, 2=领导, 3=辅导员, 4=管理员 |
| clazz.status | 1=在读, 2=撤班, 3=已毕业 |
| school.status | 1=正常, 2=解散, 3=未启用 |
| activity.status | 1=未审核, 2=审核通过, 3=审核不通过, 4=已取消, 5=举办中, 6=已结束 |
| activity.type | 1=校级思想, 2=校级文体, 3=院级思想, 4=院级文体 |
| extra_item.status / score.status | 1=未审核, 2=审核通过, 3=审核不通过 |
| receiver_type | 1=学生, 2=员工 |

---

## 八、功能需求

### 学生端（用户端）

1. **登录**：学生不能自己注册，账号由管理员预置；用学号 + 密码登录。
2. **查询个人信息**：仅查自己，含名字、性别、地址、身份证、班级、学院、专业、入学时间(年月)、手机号、邮箱、描述、已参加活动(分页)、违规记录(分页)。
3. **修改个人信息**：仅允许改**电话、邮箱、密码、描述**四项。
4. **活动列表**：查看所有审核通过的活动，分页 + 多条件筛选（校级思想/校级文体/院级思想/院级文体、已结束、举办中、已满、未满、人数范围、名称、创办学院、自己已参加、自己未参加）。
5. **活动详情与报名**：详情含自己的参加状态、可得综测分状态；可报名。满员则后来学生**无法加入**；**已参加不能退出**。
6. **申请创建活动**：学生会/社团成员可申请，填活动名称、日期、限制人数、负责老师；提交后向负责老师发消息通知。
7. **添加其他综测加分**：期末可添加（志愿活动/期末成绩等），填名称、佐证图片、分数；状态未审核，通知本班辅导员。
8. **计算综测成绩**：公式 = Σ校级活动 0.2 + Σ院级活动 0.1 + Σ已通过加分项 − Σ违规扣分。结果为"未审核"，员工审核后生效。约束：按时间排序；审核不通过可删除、已审核/未审核不可删除；一次只能有一个未审核综测；**一年最多 2 个审核通过**（后端硬拦截）。
9. **综测成绩查询**：条件分页查自己的综测。
10. **接收通知**：WebSocket 实时接收。

### 员工端（管理端）

1. 注册、登录。
2. 条件分页查学生/学院/员工/专业/活动，可看除密码外全部详情。
3. 修改所有信息。
4. 审核活动（通过/不通过+原因）。
5. 审核加分项（不通过写原因）。
6. 添加违规记录（名称、原因、扣分、处分）。
7. 查看统计（学院/专业/班级均分、板块对比、活动统计）。
8. 审核本班加分项（辅导员，不通过写原因并通知学生）。
9. 审核综测成绩（通过即生效，不通过写原因并通知）。
10. 查所有已审核综测（条件分页）。

---

## 九、定时任务（Spring Task）

1. **每 1 分钟**：活动超员则踢出**最后一个加入者**（按 `join_time` 毫秒精度），WebSocket 通知该学生 + 警报所有员工。
2. **每 1 分钟**：活动超过 `end_time` 自动置为已结束。
3. **每年 3 月 1 日、9 月 1 日**：提醒所有学生计算综测。

---

## 十、核心约束与硬性要求

1. 活动报名用**乐观锁或分布式锁**防并发超员。
2. 综测一年最多 2 个通过，后端**硬拦截**。
3. WebSocket 支持**集群广播**（Redis 发布订阅）。
4. 接口响应 ≤ 500ms（缓存加持）。
5. 代码全程中文注释。
6. BCrypt 加密；默认密码身份证后 6 位，无身份证用当前时间数字。
7. 学生/员工通过 JWT 角色拦截，学生只能访问自己权限内数据。

---

## 十一、输出要求（交付物清单）

全部中文，按顺序生成：

1. **完整可运行项目代码**（backend + frontend/admin + frontend/user），依赖完整、可直接编译启动。
2. **需求分析文档**（背景、功能清单、业务规则、表关系、约束）。
3. **API 接口文档**（Knife4j 注解 + 单独 Markdown 版接口清单）。
4. **README**（简介、技术栈、目录结构、启动步骤、数据库初始化、默认账号、部署说明）。
5. 控制台只打印关键进度。

**开始生成。**
