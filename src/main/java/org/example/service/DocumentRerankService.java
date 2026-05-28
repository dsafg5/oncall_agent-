package org.example.service;

import java.util.List;

public interface DocumentRerankService {

    List<VectorSearchService.SearchResult> rerank(
            String query,
            List<VectorSearchService.SearchResult> candidates,
            int topK
    );
}
