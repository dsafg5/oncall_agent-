# SuperBizAgent

oncall_agent- 是一个基于 Spring Boot 和 AI Agent 的智能问答与运维告警分析项目。它支持与 Agent 聊天，并结合告警、日志和内部文档生成运维处理建议。

## 项目简介

项目包含两个核心能力：

1. RAG 智能问答
   集成 Milvus 向量数据库和阿里云 DashScope，将内部文档向量化后进行相似度检索，再结合大模型生成回答。

2. AIOps 智能运维
   基于 AI Agent 的自动化运维分析流程，结合 Prometheus 告警、CLS 日志模拟数据、内部运维文档和大模型生成告警分析报告。

## 核心功能

- RAG 问答：向量检索、多轮对话、流式输出
- AIOps 运维：告警分析、日志查询、处理建议生成
- 工具集成：时间工具、内部文档检索、Prometheus 告警查询、CLS 日志查询
- 文件上传：上传文档后自动切片、向量化并写入 Milvus
- Web 界面：提供浏览器交互页面和 REST API

## 技术栈

| 技术 | 说明 |
| --- | --- |
| Java 17 | 开发语言 |
| Spring Boot 3.2.0 | 应用框架 |
| Spring AI | AI 工具调用与 Agent 集成 |
| DashScope | 阿里云大模型与 Embedding 服务 |
| Milvus | 向量数据库 |
| Prometheus | 告警和指标来源 |
| Tencent CLS / Mock | 日志查询来源或模拟日志 |

## 模块结构

```text
src/main/java/org/example/
  controller/              API 控制器
  service/                 核心业务服务
  agent/tool/              Agent 工具
  config/                  Spring 配置
  client/                  Milvus 客户端工厂
  constant/                常量定义
  dto/                     数据传输对象

src/main/resources/
  application.yml          应用配置
  static/                  Web 前端页面

aiops-docs/                运维知识文档
vector-database.yml        Milvus Docker Compose 配置
```

## 数据源说明

Prometheus 用于查询当前告警和指标，例如 CPU 高、内存高、服务不可用、响应慢。

CLS 日志工具当前支持 mock 模式。`cls.mock-enabled=true` 时，系统返回代码中模拟的日志数据，例如应用错误、慢请求、数据库慢查询、Pod 重启和 OOM 事件。`cls.mock-enabled=false` 时，本地真实 CLS 查询尚未实现，需要接入腾讯云 MCP 或实现 CLS API 调用。

Milvus 用于保存内部文档向量。大模型调用 RAG 工具时，会先把问题转成向量，再查询 Milvus 中的 `biz` collection。

DashScope 用于大模型对话和文本向量化。

## 配置

主要配置文件：

```text
src/main/resources/application.yml
```

DashScope API Key 不应写入代码仓库。运行前请通过环境变量配置：

```powershell
$env:DASHSCOPE_API_KEY="your-api-key"
```

永久设置：

```powershell
setx DASHSCOPE_API_KEY "your-api-key"
```

关键配置示例：

```yaml
server:
  port: 9900

milvus:
  host: localhost
  port: 19530

spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:}

dashscope:
  api:
    key: ${DASHSCOPE_API_KEY:}
  embedding:
    model: text-embedding-v4

rag:
  top-k: 3
  model: "qwen3-max"

prometheus:
  base-url: http://localhost:9090
  timeout: 10
  mock-enabled: true

cls:
  mock-enabled: true
```

## 启动

启动 Milvus：

```bash
docker compose -f vector-database.yml up -d
```

启动应用：

```bash
mvn spring-boot:run
```

访问 Web 页面：

```text
http://localhost:9900
```

也可以使用 Makefile 初始化：

```bash
make init
```

## API

流式聊天：

```bash
POST /api/chat_stream
Content-Type: application/json

{
  "Id": "session-123",
  "Question": "什么是向量数据库？"
}
```

普通聊天：

```bash
POST /api/chat
Content-Type: application/json

{
  "Id": "session-123",
  "Question": "什么是向量数据库？"
}
```

AIOps 分析：

```bash
POST /api/ai_ops
```

上传文档：

```bash
POST /api/upload
```

Milvus 健康检查：

```bash
GET /milvus/health
```

## 注意事项

- 不要提交真实 API Key。
- `target/`、`.idea/`、`volumes/`、`uploads/` 已通过 `.gitignore` 忽略。
- 如果 Milvus 重启后 `biz` collection 未加载，搜索前会自动调用 `loadCollection`。
- 如果需要真实腾讯云 CLS 查询，需要启用并配置 MCP，或实现真实 CLS API 查询逻辑。

## License

MIT
