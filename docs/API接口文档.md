# 学生综测统计系统 · API 接口文档

## 约定

- 基础路径：`/api/v1`
- 统一响应：`{ code, msg, data }`，`code=1` 成功、`code=0` 业务失败、`401` 未登录、`403` 无权限、`429` 触发限流（HTTP 200，见 `msg`）
- 鉴权方式：请求头 `Authorization: Bearer <token>`
- 在线文档：后端启动后访问 `http://localhost:8080/doc.html`

### 列表查询通用规则

> 前端 UI 为「逐字段独立控件」：文本业务字段用输入框模糊搜索，主键/外键/状态/类型/学年/学期等标识与枚举字段用下拉精确匹配（一律不手输数字 id）。

- **文本业务字段**：`LIKE %关键字%` 模糊搜索（如姓名、学号/工号、电话、名称、原因、描述等）。
- **主键 / 外键 / 枚举 / 数值标识**（`id`、`schoolId`、`majorId`、`clazzId`、`studentId`、`status`、`type`、`role`、`year`、`semester`、`full`、`joined` 等）：仅 `=` 精确匹配，**禁止模糊搜索**。
- 全部参数均为**可选**：不传即不过滤；模糊参数传空串等同不传。
- 分页参数：`page`（默认 1）、`size`（默认 10）。

## 一、认证接口

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/auth/student/login` | 学生登录 | 公开 |
| POST | `/auth/employee/login` | 员工登录 | 公开 |
| POST | `/auth/employee/register` | 员工注册 | 公开 |
| GET | `/auth/info` | 当前登录用户信息 | 需登录 |
| GET | `/captcha` | 获取图形验证码（`data.captchaId` + Base64 图片） | 公开（IP 限流） |

### 学生登录

先请求 `GET /captcha` 取得 `captchaId` 与图片，再登录：

请求：
```json
{ "username": "20220101", "password": "011234", "captchaId": "xxx", "captchaCode": "abcd" }
```

> 验证码**一次性**：每次校验后即失效；登录失败后前端必须重新获取验证码。同一账号连续失败 5 次锁定 15 分钟（每次失败记录 10 分钟内有效）。验证码获取接口按 IP 限流（60 秒 30 次）。

响应 `data`：
```json
{ "token": "xxx", "userId": 1, "userType": "student", "role": 1, "username": "20220101", "name": "张三" }
```

## 二、学生端接口（需学生 token）

### 个人信息

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/student/profile` | 查询个人信息（含班级/学院/专业名） |
| PUT | `/student/profile` | 修改个人信息（仅电话/邮箱/密码/描述） |

### 活动

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/student/activities` | 活动列表（分页+筛选） |
| GET | `/student/activities/{id}` | 活动详情（含我的参加状态、可得分） |
| POST | `/student/activities/{id}/join` | 报名活动 |
| POST | `/student/activities` | 申请创建活动（学生会/社团） |

活动列表查询参数：`name` **模糊**；`type`(1-4)、`schoolId`、`status`(2举办中/6已结束)、`full`(1已满/2未满)、`joined`(1已参加/2未参加)、`minNum`、`maxNum` 均为**精确**过滤。

创建活动请求体：
```json
{ "name": "活动名", "type": 2, "schoolId": null, "limitNum": 50,
  "startTime": "2026-09-10T10:00:00", "endTime": "2026-09-10T18:00:00",
  "description": "描述", "empInChargeIds": [1], "studentInChargeIds": [] }
```

### 综测加分项

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/student/extra-items` | 添加加分项（通知辅导员） |
| GET | `/student/extra-items` | 查询我的加分项（分页） |

### 综测成绩

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/student/scores?year=&semester=` | 计算综测成绩 |
| GET | `/student/scores` | 查询我的综测（分页，可按 status 筛） |
| DELETE | `/student/scores/{id}` | 删除审核不通过的综测 |

### 违规记录 / 通知

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/student/penalties` | 我的违规记录（分页） |
| GET | `/student/notifications` | 我的通知（分页） |
| PUT | `/student/notifications/{id}/read` | 标记通知已读 |

## 三、员工端接口（需员工 token）

### 学生管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/students` | 分页查询（name/username/clazzId/schoolId/status/role） |
| GET | `/employee/students/{id}` | 详情 |
| POST | `/employee/students?majorIds=` | 新增（默认密码=身份证后6位） |
| PUT | `/employee/students?majorIds=` | 修改 |
| DELETE | `/employee/students/{id}` | 删除 |
| PUT | `/employee/students/{id}/reset-password` | 重置密码 |

### 员工管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/employees` | 分页查询 |
| GET | `/employee/employees/{id}` | 详情 |
| POST | `/employee/employees?schoolIds=` | 新增 |
| PUT | `/employee/employees?schoolIds=` | 修改 |
| DELETE | `/employee/employees/{id}` | 删除 |

### 学院 / 专业 / 班级管理

| 资源 | 列表 | 新增 | 修改 | 删除 |
| --- | --- | --- | --- | --- |
| 学院 schools | GET `/employee/schools` | POST `/employee/schools` | PUT `/employee/schools` | DELETE `/employee/schools/{id}` |
| 专业 majors | GET `/employee/majors` | POST `/employee/majors` | PUT `/employee/majors` | DELETE `/employee/majors/{id}` |
| 班级 clazzs | GET `/employee/clazzs` | POST `/employee/clazzs` | PUT `/employee/clazzs` | DELETE `/employee/clazzs/{id}` |

### 活动管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/activities` | 分页查询 |
| GET | `/employee/activities/{id}` | 详情 |
| PUT | `/employee/activities` | 修改 |
| PUT | `/employee/activities/{id}/review` | 审核（`{approve, reason}`） |

### 加分项审核

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/extra-items` | 分页查询全部 |
| PUT | `/employee/extra-items/{id}/review` | 审核 |
| GET | `/employee/my-class/extra-items` | 本班加分项（辅导员） |
| PUT | `/employee/my-class/extra-items/{id}/review` | 审核本班加分项 |

### 违规记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/employee/penalties` | 添加违规记录 |
| GET | `/employee/penalties` | 分页查询 |

### 综测成绩

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/scores/approved` | 所有已审核通过的综测 |
| GET | `/employee/scores/pending` | 待审核综测 |
| PUT | `/employee/scores/{id}/review` | 审核（`{approve, reason}`） |

### 统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/statistics` | 学院/专业/班级均分、板块对比、活动统计 |

响应 `data` 示例：
```json
{
  "schoolAverages": [{ "id": 1, "name": "计算机学院", "avg": 1.50 }],
  "majorAverages": [{ "id": 1, "name": "软件工程", "avg": 1.50 }],
  "clazzAverages": [{ "id": 1, "name": "软件2201", "avg": 1.50 }],
  "boardComparison": { "活动平均分": 1.00, "加分平均分": 0.50, "扣分平均分": 0.00, "总平均分": 1.50 },
  "activityStats": { "活动总数": 2, "未审核": 0, "审核通过": 2, "审核不通过": 0, "已结束": 0, "校级活动": 1, "院级活动": 1 }
}
```

### 通知

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/employee/notifications` | 我的通知（分页） |
| PUT | `/employee/notifications/{id}/read` | 标记已读 |

## 四、文件上传

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/file/upload` | 上传佐证图片（multipart，字段 `file`），返回访问 URL |

## 五、WebSocket

- 连接地址：`ws://host/ws?token=<jwt>`
- 服务端按用户推送通知，消息体为 JSON：
```json
{ "receiverId": 1, "receiverType": 1, "title": "标题", "content": "内容", "type": 1, "relatedId": null }
```
