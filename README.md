# Calendar · 语音日历 VoiceCal

> 以语音交互为核心的日历管理工具：**开口即记、说完即成**。
> 一句「我明天下午三点到五点有个会议」即可完成添加，并支持语音查看 / 修改 / 删除 / 提醒。

## 交付物清单

| 文件 | 内容 |
| --- | --- |
| [`docs/01-需求文档(PRD).md`](docs/01-需求文档(PRD).md) | 完整产品需求文档：背景、用户研究、功能清单（带编号）、流程、**页面⇄功能映射总表**、验收标准 |
| [`docs/02-数据库设计文档.md`](docs/02-数据库设计文档.md) | MySQL 表结构（DDL）、E-R 关系、**功能→数据库表映射**、关键 SQL |
| [`docs/03-页面设计.md`](docs/03-页面设计.md) | 每个页面布局 + **每个按钮对应的需求编号** + 跳转关系 |
| [`docs/05-接口文档.md`](docs/05-接口文档.md) | RESTful 接口（Vue + Spring Boot）、**功能→接口→数据库表映射**、响应与表字段一一对应 |
| [`docs/04-功能标注图.md`](docs/04-功能标注图.md) | **7 张页面功能标注图**：每页截图 + 箭头标注每个控件对应的需求编号 |
| `docs/页面标注-P1~P7-*.png` | 各页面功能标注图（图片） |
| [`prototype/index.html`](prototype/index.html) | 可点击高保真原型（含全部页面 P1–P7） |
| `prototype/annotate.html` | 功能标注图生成器（`?p=home/confirm/...` 切换页面） |

## 后端实现进度（Spring Boot + MyBatis）

| 模块 | 接口 | 状态 | 操作表 |
| --- | --- | --- | --- |
| 语音解析 | `POST /api/voice/parse` | ✅ 已实现 | `voice_log` |
| 事件管理 | `POST/GET/PUT/DELETE /api/events`、`/conflicts`、`/restore` | ✅ 已实现 | `event`、`reminder` |
| 提醒管理 | `GET /api/reminders/upcoming` | ✅ 已实现 | `reminder`、`event` |
| 用户设置 | `/api/settings`、`DELETE /api/voice-logs` | 待开发 | `user`、`voice_log` |

技术栈：**Spring Boot 3.3.2**、**MyBatis 3.0.3**、**MySQL 8.0**、JDK 17。统一响应格式见 [`docs/05-接口文档.md`](docs/05-接口文档.md) 第 0.1 节（`code` / `msg` / `data`）。

### 事件管理模块说明

对应文档 **第 2 节**，已实现全部 7 个接口：

| 接口 | 说明 |
| --- | --- |
| `POST /api/events` | 创建事件；冲突检测；写入 `reminder` |
| `GET /api/events` | 时间范围 / 关键词分页列表 |
| `GET /api/events/{id}` | 详情（含 `reminders`） |
| `PUT /api/events/{id}` | 部分字段更新；可重算提醒 |
| `DELETE /api/events/{id}` | 逻辑删除（`status=0`） |
| `PUT /api/events/{id}/restore` | 撤销删除 |
| `GET /api/events/conflicts` | 时间冲突检测 |

主要代码：`controller/EventController.java`、`service/impl/EventServiceImpl.java`、`mapper/EventMapper.xml`。

### 提醒管理模块说明

对应文档 **第 3 节**，已实现：

| 接口 | 说明 |
| --- | --- |
| `GET /api/reminders/upcoming` | 查询未来 N 小时内即将到来的提醒（`hours` 默认 24） |

`reminder` 联表 `event`（`event.status=1`），按 `fire_time` 升序返回 `id`、`eventId`、`eventTitle`、`startTime`、`offsetMinutes`、`fireTime`、`sent`。

主要代码：`controller/ReminderController.java`、`service/impl/ReminderServiceImpl.java`、`mapper/ReminderMapper.xml`（`selectUpcoming`）。

```bash
curl "http://localhost:8080/api/reminders/upcoming?hours=24"
```

### 语音解析模块说明

对应需求 **F-VOICE-01/02/03**，接口详见文档 **1.1 语音 / 文本解析**：

- **路径**：`POST /api/voice/parse`
- **行为**：接收语音转写或键盘输入的自然语言，解析为结构化 JSON 供前端确认卡展示；**仅写入 `voice_log`，不创建事件**（先确认后保存）。
- **解析能力**：默认使用 **DeepSeek V4**（`deepseek-v4-pro`）将自然语言译为结构化 JSON；API Key 写在本地 `application-local.yml`（**不会提交 GitHub**）。LLM 失败时可回退规则解析器（`voicecal.llm.fallback-to-rules`）。
- **主要代码**：
  - `controller/VoiceController.java` — REST 入口
  - `llm/DeepSeekVoiceParser.java` — DeepSeek V4 解析
  - `llm/DeepSeekClient.java` — DeepSeek HTTP 客户端
  - `service/NaturalLanguageParser.java` — 规则解析（LLM 回退）
  - `service/impl/VoiceParseServiceImpl.java` — 编排解析与落库
  - `mapper/VoiceLogMapper.xml` — `voice_log` 插入

### 后端快速启动

```bash
cd Calendar
mvn spring-boot:run    # 默认端口 8080；需本机或集群内可访问 MySQL
```

数据库连接在 `src/main/resources/application.yml` 中配置（`spring.datasource.*`）。演示环境未接入登录，写 `voice_log` 使用 `voicecal.default-user-id`（默认 `1`）。

### DeepSeek API Key（本地配置，勿提交仓库）

```bash
cd Calendar/src/main/resources
cp application-local.yml.example application-local.yml
# 编辑 application-local.yml，填入 voicecal.llm.api-key
```

`application-local.yml` 已在 `.gitignore` 中忽略；仓库仅保留 `application-local.yml.example` 模板。克隆仓库后需自行创建本地文件才能启用 LLM 解析。

### 接口调用示例

```bash
curl -X POST http://localhost:8080/api/voice/parse \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "我明天下午三点到五点有个会议，地点在3号会议室，提前15分钟提醒",
    "now": "2026-05-31T09:56:00+08:00",
    "tz": "Asia/Shanghai"
  }'
```

成功时返回 `code: 1`，`data` 含 `intent`、`title`、`startTime`、`endTime`、`location`、`reminderOffsets`、`confidence` 等字段（与接口文档样例一致）。

## 快速开始（前端原型）

直接用浏览器打开原型即可体验（无需后端，内置模拟解析）：

```bash
open prototype/index.html      # macOS
```

在原型里点击中央 🎤 按钮，可体验完整链路：
**语音 → 模拟 LLM 解析 → 解析结果确认卡 → 保存 → 今日视图刷新**。
（依次演示「添加 / 修改 / 删除 / 查询」四种意图。）

联调后端时，将前端 Axios 的 baseURL 指向 `http://localhost:8080`，调用 `POST /api/voice/parse` 即可替换原型内的模拟解析。

## 核心设计主张

1. **语音为核心**：首页中央醒目麦克风按钮，全局可达。
2. **先确认后保存**：语音难免误识别，所有增删改都先过「解析结果确认卡」，低置信度字段黄色高亮。
3. **LLM 只做翻译，服务端做可信计算**：大模型把自然语言译成结构化 JSON（意图+字段），时间换算、冲突检测、落库都在服务端。
4. **可逆**：删除等破坏性操作支持 5 秒撤销。

## 需求 ⇄ 页面对应

PRD 第 7 节给出完整映射总表；原型中每个控件都带有蓝色需求编号小标签（如 `F-VOICE-01`），可与文档逐一核对。

> 说明：为方便点击体验，原型把 P1–P7 全部页面集成在单个 `index.html` 中通过页内路由切换；文档中按页面（P1–P7）分别描述。

## 仓库

GitHub：[https://github.com/ChinanBoys/Calendar](https://github.com/ChinanBoys/Calendar)
