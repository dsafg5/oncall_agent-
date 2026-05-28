# RAG Rerank Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add mandatory Alibaba Cloud Bailian/DashScope reranking after Milvus recall and document the change in the changelog.

**Architecture:** Keep Milvus as the first-stage vector retriever, then rerank the recalled candidates with `qwen3-rerank` before returning the final RAG context. Rerank failures are fatal and never fall back to Milvus ordering.

**Tech Stack:** Java 17, Spring Boot, OkHttp, Jackson, Milvus SDK, DashScope/Bailian rerank HTTP API.

---

### Task 1: Rerank Contract

**Files:**
- Create: `src/main/java/org/example/service/DocumentRerankService.java`
- Create: `src/test/java/org/example/service/VectorSearchServiceRerankTest.java`

- [ ] Write a failing test proving `VectorSearchService` asks a reranker to reorder Milvus results.
- [ ] Introduce `DocumentRerankService` as a small interface returning reranked `SearchResult` values.
- [ ] Inject the reranker into `VectorSearchService`.

### Task 2: Rerank HTTP Client

**Files:**
- Create: `src/main/java/org/example/service/DashScopeRerankService.java`
- Modify: `src/main/resources/application.yml`

- [ ] Add config for `rag.recall-top-k`, `rag.rerank.model`, `rag.rerank.endpoint`, and `rag.rerank.enabled`.
- [ ] Implement the DashScope/Bailian HTTP request using `Authorization: Bearer ${DASHSCOPE_API_KEY}`.
- [ ] Parse `output.results[].index` and `output.results[].relevance_score`.

### Task 3: Mandatory Failure Behavior

**Files:**
- Modify: `src/main/java/org/example/service/VectorSearchService.java`
- Test: `src/test/java/org/example/service/VectorSearchServiceRerankTest.java`

- [ ] Add a failing test proving rerank errors are propagated.
- [ ] Ensure no fallback to Milvus ordering is returned when rerank fails.

### Task 4: Documentation and Verification

**Files:**
- Create or Modify: `CHANGELOG.md`

- [ ] Document what changed, the new data flow, config, failure behavior, and maintenance notes.
- [ ] Run `mvn test`.
- [ ] Commit and push the implementation.
