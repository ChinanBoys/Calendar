# J人神器

> 以语音交互为核心的日历管理工具：**开口即记、说完即成**。
> 一句「我明天下午三点到五点有个会议」即可完成添加，并支持语音查看 / 修改 / 删除 / 提醒。

## 参赛作品 Demo 视频

**参赛作品 demo 展示 · 小羊走刀口**（小红书）

🔗 [点击观看 Demo 视频](https://www.xiaohongshu.com/discovery/item/6a1c59e6000000003601a674?source=webshare&xhsshare=pc_web&xsec_token=ABBBefrz-TsWUh2yD_hI0Lv7CHySYyNdQ1r5bQknndGUU=&xsec_source=pc_share)

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
| [`prototype/annotate.html`](prototype/annotate.html) | 功能标注图生成器（`?p=home/confirm/...` 切换页面） |
| [`fronter/`](fronter/) | **Vue 3 前端工程**：P1 今日视图已实现，Axios 对接 `/api` |

## 后端实现进度（Spring Boot + MyBatis）

| 模块 | 接口 | 状态 | 操作表 |
| --- | --- | --- | --- |
| 语音解析 | `POST /api/voice/parse` | ✅ 已实现 | `voice_log` |
| 事件管理 | `POST/GET/PUT/DELETE /api/events`、`/conflicts`、`/restore` | ✅ 已实现 | `event`、`reminder` |
| 提醒管理 | `GET /api/reminders/upcoming` | ✅ 已实现 | `reminder`、`event` |
| 用户设置 | `GET/PUT /api/settings`、`DELETE /api/voice-logs` | ✅ 已实现 | `user`、`voice_log` |

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

### 用户设置模块说明

对应文档 **第 4 节**，已实现：

| 接口 | 说明 |
| --- | --- |
| `GET /api/settings` | 查询偏好：`timezone`、`defaultReminderMinutes`、`notifyEnabled`、`voiceRetention` |
| `PUT /api/settings` | 部分更新上述字段（`voiceRetention` 限 none/7d/forever） |
| `DELETE /api/voice-logs` | 清除当前用户全部 `voice_log` 记录 |

主要代码：`controller/SettingsController.java`、`controller/VoiceLogController.java`、`service/impl/SettingsServiceImpl.java`、`mapper/UserMapper.xml`。

```bash
curl http://localhost:8080/api/settings
curl -X PUT http://localhost:8080/api/settings -H 'Content-Type: application/json' \
  -d '{"defaultReminderMinutes":30,"notifyEnabled":true,"voiceRetention":"none"}'
curl -X DELETE http://localhost:8080/api/voice-logs
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

## 前端实现进度（Vue 3 + Element Plus）

| 页面 | 路径 | 状态 | 说明 |
| --- | --- | --- | --- |
| P1 首页 / 今日视图 | `fronter/src/views/TodayView.vue` | ✅ 布局完成 | 手机框、时间轴、语音交互区；点击事件进 P4 |
| P2 解析结果确认卡 | `fronter/src/components/today/ConfirmCard.vue` | ✅ 布局完成 | 语音/文本解析后底部弹出；含冲突提示与保存 |
| P3 日历（周/月） | `fronter/src/views/CalendarView.vue` | ✅ 布局完成 | 月历网格、当日安排、悬浮 🎤；从 P1 Tab 进入 |
| P4 事件详情 / 编辑 | `fronter/src/views/EventDetailView.vue` | ✅ 布局完成 | 表单字段、保存/删除；从 P1 事件卡进入 |
| P5 提醒中心 | `fronter/src/views/RemindersView.vue` | ✅ 布局完成 | 即将到来 + 历史提醒；从 P1 🔔 进入 |
| P6 搜索 | `fronter/src/views/SearchView.vue` | ✅ 布局完成 | 搜索框 + 结果列表；从 P1 🔍 进入 |
| P7 设置 | `fronter/src/views/SettingsView.vue` | ✅ 布局完成 | 提醒/通用/隐私/账户分组；从 P1 ☰ 进入 |

技术栈：**Vue 3**、**Element Plus**、**Axios**、**Vue Router 4**、**Vite 5**。设计依据 `docs/页面标注-P1~P7-*.png` 与 [`docs/03-页面设计.md`](docs/03-页面设计.md)；各页仅渲染左侧手机框 UI，不含标注箭头与右侧功能标签。**当前 P1–P7 均已完成布局，接口对接待后续迭代。**

### P2 解析确认卡布局

语音/文本解析（`intent=create/update/delete`）后，在 P1 手机框内自底部弹出：标题栏、红色冲突提示条、原话、字段列表（低置信度黄标）、重说/手动修改/确认保存/取消。

### P3 日历视图布局

顶栏（返回 + 月份 + 周/月切换）、月历/周历网格（选中蓝圈、事件橙点）、「X月X日 安排」列表、右下角悬浮 🎤。

### P4 事件详情布局

顶栏（返回 + 标题 + 🎤改）、标题/全天/起止时间/地点/提醒/重复/备注表单、「保存修改」「删除此事件」按钮。

### P5 提醒中心布局

顶栏「提醒」、即将到来与历史提醒两组卡片（红色闹钟图标 + 标题 + 副标题）。

### P6 搜索布局

返回 + 圆角搜索框（含 🎤）、「结果」列表（左日期时间、右标题地点）。

### P7 设置布局

顶栏「设置」、提醒/通用/隐私/账户四组设置项（白底圆角 cell + 右箭头）。

### P1 今日视图布局

| 区域 | 内容 |
| --- | --- |
| **顶栏** | ☰ 菜单、日期标题「今日 · X月X日 周X」、副标题 VoiceCal 语音日历、🔍 搜索、🔔 提醒 |
| **Tab** | 今日 / 周 / 月（胶囊切换，今日为靛蓝 `#5B6CFF` 激活态） |
| **时间轴** | 「今日时间轴」+ 事件卡片（左起止时间、右标题/地点；冲突事件红边 + 黄色「时间冲突」标记） |
| **底部语音区** | 识别文字、中央 🎤 按钮、「⌨ 输入」「我有什么安排」快捷按钮 |

### P1 控件 → 接口映射

| 控件 | 功能编号 | 接口 |
| --- | --- | --- |
| ☰ 菜单 | F-SET | `GET /api/settings`（跳转 P7 设置页） |
| 🔍 搜索 | F-VIEW-05 | `GET /api/events?keyword=` |
| 🔔 铃铛 | F-REMIND-01 | `GET /api/reminders/upcoming` |
| 今日 Tab + 时间轴 | F-VIEW-01 | `GET /api/events?from=&to=` |
| 周 / 月 Tab | F-VIEW-02 | `GET /api/events?from=&to=`（跳转 P3） |
| 事件卡片点击 | F-VIEW-04 | `GET /api/events/{id}` |
| 左滑删除 | F-DEL-02 | `DELETE /api/events/{id}` + `PUT /api/events/{id}/restore` 撤销 |
| 冲突标记 | F-CONFLICT-01 | 前端检测时间重叠（保存前亦可用 `GET /api/events/conflicts`） |
| 🎤 语音按钮 | F-VOICE-01 | `POST /api/voice/parse` |
| ⌨ 输入 | F-VOICE-02 | `POST /api/voice/parse` |
| 我有什么安排 | F-VIEW-03 | `POST /api/voice/parse` → `GET /api/events` |

### 前端目录结构

```
fronter/src/
├── api/                    # Axios 封装：events、voice、reminders、settings
├── components/today/       # EventCard、VoicePanel、ConfirmCard
├── components/calendar/    # AgendaEventItem
├── views/                  # P1–P7 全部页面
├── router/                 # Vue Router 路由
├── utils/                  # 日期、日历、冲突检测
└── mock/                   # 各页演示数据（布局预览）
```

### 前端快速启动

```bash
cd Calendar/fronter
npm install
npm run dev          # 默认 http://localhost:3000，/api 代理至 http://localhost:8080
```

生产构建：

```bash
cd Calendar/fronter
npm run build        # 产物输出至 fronter/dist/
npm run preview      # 本地预览构建结果
```

Vite 开发代理配置见 `fronter/vite.config.js`：`/api` → `http://localhost:8080`。需先启动后端（`mvn spring-boot:run`）方可联调真实数据；开发模式下接口失败时会回退 `mock/demoEvents.js` 演示数据以预览布局。

## 快速开始（HTML 原型）

直接用浏览器打开原型即可体验（无需后端，内置模拟解析）：

```bash
open prototype/index.html      # macOS
```

在原型里点击中央 🎤 按钮，可体验完整链路：
**语音 → 模拟 LLM 解析 → 解析结果确认卡 → 保存 → 今日视图刷新**。
（依次演示「添加 / 修改 / 删除 / 查询」四种意图。）

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
