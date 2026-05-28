# 更新日志

## 2026-05-28 - RAG 文档召回后接入百炼重排模型

### 背景

原来的 RAG 查询流程只有一阶段召回：

```text
用户问题
-> DashScope Embedding 生成问题向量
-> Milvus 在 biz collection 中按向量距离召回 topK 文档
-> queryInternalDocs 把 Milvus 结果返回给大模型
```

这个流程的问题是：Milvus 的向量距离适合快速粗召回，但不一定最适合最终排序。比如两个文档都和问题相近，向量距离更近的文档不一定更能直接回答问题。因此新增第二阶段重排：

```text
用户问题
-> DashScope Embedding 生成问题向量
-> Milvus 先召回更多候选文档
-> 百炼 qwen3-rerank 对「问题 + 候选文档」重新打分排序
-> 只返回重排后的 topK 文档给大模型
```

### 本次改动

新增 `DocumentRerankService` 接口：

```text
src/main/java/org/example/service/DocumentRerankService.java
```

它定义了重排服务的最小契约：

```java
List<VectorSearchService.SearchResult> rerank(
        String query,
        List<VectorSearchService.SearchResult> candidates,
        int topK
);
```

新增 `DashScopeRerankService`：

```text
src/main/java/org/example/service/DashScopeRerankService.java
```

它通过 HTTP 调用阿里云百炼兼容接口：

```text
POST https://dashscope.aliyuncs.com/compatible-api/v1/reranks
```

请求使用环境变量中的 DashScope API Key：

```text
Authorization: Bearer ${DASHSCOPE_API_KEY}
```

请求体核心字段：

```json
{
  "model": "qwen3-rerank",
  "query": "用户问题",
  "documents": ["候选文档1", "候选文档2"],
  "top_n": 3,
  "return_documents": false
}
```

返回结果中读取：

```text
results[].index
results[].relevance_score
```

`index` 表示候选文档在原始 documents 列表中的下标，`relevance_score` 是重排模型给出的相关性分数。

### RAG 查询链路变化

修改位置：

```text
src/main/java/org/example/service/VectorSearchService.java
```

旧逻辑：

```text
Milvus topK -> 直接返回
```

新逻辑：

```text
Milvus recallTopK -> DashScope qwen3-rerank -> rerank topK -> 返回
```

关键点：

1. `rag.top-k` 仍然表示最终返回给大模型的文档数量。
2. `rag.recall-top-k` 表示 Milvus 第一阶段召回的候选数量。
3. `recall-top-k` 应该大于或等于 `top-k`，否则没有足够候选给重排模型筛选。
4. `SearchResult` 新增 `rerankScore`，用于记录百炼重排分数。

### 配置项

配置位置：

```text
src/main/resources/application.yml
```

新增配置：

```yaml
rag:
  top-k: 3
  recall-top-k: 10
  model: "qwen3-max"
  rerank:
    enabled: true
    model: qwen3-rerank
    endpoint: https://dashscope.aliyuncs.com/compatible-api/v1/reranks
    timeout: 30
```

配置含义：

| 配置 | 含义 |
| --- | --- |
| `rag.top-k` | 最终返回给大模型的文档数量 |
| `rag.recall-top-k` | Milvus 第一阶段召回候选文档数量 |
| `rag.rerank.enabled` | 是否启用重排 |
| `rag.rerank.model` | 百炼重排模型名称 |
| `rag.rerank.endpoint` | 百炼重排接口地址 |
| `rag.rerank.timeout` | 重排 HTTP 请求超时时间，单位秒 |

### 不降级策略

本次实现遵循“不降级”要求。

也就是说：

```text
Milvus 召回成功
-> 百炼 qwen3-rerank 调用失败
-> 整个 RAG 查询失败
-> 不返回 Milvus 原始排序结果
```

这样做的原因是：如果用户要求结果必须经过重排，那么在重排失败时继续返回 Milvus 原始排序，会让调用方误以为结果已经经过重排，属于静默降级。

当前错误会从 `DashScopeRerankService` 抛到 `VectorSearchService`，再被 `InternalDocsTools.queryInternalDocs` 捕获并返回 error JSON 给大模型。

### 测试

新增测试：

```text
src/test/java/org/example/service/VectorSearchServiceRerankTest.java
```

覆盖两个行为：

1. `VectorSearchService` 会调用 `DocumentRerankService`，并返回重排后的 topK。
2. `DocumentRerankService` 抛错时，`VectorSearchService` 不会退回 Milvus 原始排序。

已有测试：

```text
src/test/java/org/example/service/VectorSearchServiceTest.java
```

继续验证搜索前会加载 Milvus collection，避免 `collection not loaded` 问题。

### 维护说明

如果以后要更换重排模型，只改配置：

```yaml
rag:
  rerank:
    model: 新模型名
```

如果百炼接口返回结构变化，需要修改：

```text
DashScopeRerankService.parseRerankResponse(...)
```

如果要调优召回效果，优先调整：

```yaml
rag:
  recall-top-k: 10
  top-k: 3
```

常见建议：

```text
top-k = 3
recall-top-k = 10 到 20
```

`recall-top-k` 越大，重排模型看到的候选越多，结果可能更准，但接口耗时和费用也会增加。
