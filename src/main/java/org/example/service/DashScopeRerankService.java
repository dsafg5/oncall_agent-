package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashScopeRerankService implements DocumentRerankService {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeRerankService.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${dashscope.api.key}")
    private String apiKey;

    @Value("${rag.rerank.model:qwen3-rerank}")
    private String model;

    @Value("${rag.rerank.endpoint:https://dashscope.aliyuncs.com/compatible-api/v1/reranks}")
    private String endpoint;

    @Value("${rag.rerank.timeout:30}")
    private int timeoutSeconds;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("请设置环境变量 DASHSCOPE_API_KEY 以启用百炼重排模型");
        }

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .readTimeout(Duration.ofSeconds(timeoutSeconds))
                .writeTimeout(Duration.ofSeconds(timeoutSeconds))
                .callTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        logger.info("DashScope rerank service initialized, model: {}, endpoint: {}", model, endpoint);
    }

    @Override
    public List<VectorSearchService.SearchResult> rerank(
            String query,
            List<VectorSearchService.SearchResult> candidates,
            int topK
    ) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("重排 query 不能为空");
        }
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        try {
            List<String> documents = candidates.stream()
                    .map(VectorSearchService.SearchResult::getContent)
                    .toList();

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("query", query);
            payload.put("documents", documents);
            payload.put("top_n", Math.min(topK, candidates.size()));
            payload.put("return_documents", false);

            String requestJson = objectMapper.writeValueAsString(payload);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestJson, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("百炼重排请求失败: HTTP " + response.code() + ", body: " + responseBody);
                }

                return parseRerankResponse(responseBody, candidates, topK);
            }
        } catch (Exception e) {
            logger.error("百炼重排失败", e);
            throw new RuntimeException("百炼重排失败: " + e.getMessage(), e);
        }
    }

    private List<VectorSearchService.SearchResult> parseRerankResponse(
            String responseBody,
            List<VectorSearchService.SearchResult> candidates,
            int topK
    ) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode resultsNode = root.path("output").path("results");
        if (!resultsNode.isArray()) {
            resultsNode = root.path("results");
        }
        if (!resultsNode.isArray()) {
            throw new RuntimeException("百炼重排返回结果缺少 results 字段");
        }

        List<VectorSearchService.SearchResult> reranked = new ArrayList<>();
        for (JsonNode item : resultsNode) {
            int index = item.path("index").asInt(-1);
            if (index < 0 || index >= candidates.size()) {
                throw new RuntimeException("百炼重排返回非法文档索引: " + index);
            }

            VectorSearchService.SearchResult result = candidates.get(index);
            result.setRerankScore(item.path("relevance_score").asDouble());
            reranked.add(result);
        }

        reranked.sort(Comparator.comparing(
                VectorSearchService.SearchResult::getRerankScore,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        int finalSize = Math.min(topK, reranked.size());
        return new ArrayList<>(reranked.subList(0, finalSize));
    }
}
