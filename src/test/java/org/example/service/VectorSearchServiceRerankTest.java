package org.example.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VectorSearchServiceRerankTest {

    @Test
    void rerankCandidatesReturnsRerankedTopK() throws Exception {
        DocumentRerankService rerankService = mock(DocumentRerankService.class);
        VectorSearchService service = new VectorSearchService();
        setField(service, "documentRerankService", rerankService);
        setField(service, "rerankEnabled", true);

        VectorSearchService.SearchResult low = searchResult("doc-1", "low relevance", 0.9f);
        VectorSearchService.SearchResult high = searchResult("doc-2", "high relevance", 0.4f);
        List<VectorSearchService.SearchResult> candidates = List.of(low, high);

        when(rerankService.rerank("cpu high", candidates, 1)).thenReturn(List.of(high));

        List<VectorSearchService.SearchResult> result = service.rerankCandidates("cpu high", candidates, 1);

        assertEquals(1, result.size());
        assertSame(high, result.get(0));
        verify(rerankService).rerank("cpu high", candidates, 1);
    }

    @Test
    void rerankCandidatesDoesNotFallbackWhenRerankFails() throws Exception {
        DocumentRerankService rerankService = mock(DocumentRerankService.class);
        VectorSearchService service = new VectorSearchService();
        setField(service, "documentRerankService", rerankService);
        setField(service, "rerankEnabled", true);

        List<VectorSearchService.SearchResult> candidates = List.of(
                searchResult("doc-1", "candidate", 0.7f)
        );

        when(rerankService.rerank("cpu high", candidates, 1))
                .thenThrow(new RuntimeException("rerank unavailable"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.rerankCandidates("cpu high", candidates, 1));

        assertEquals("rerank unavailable", thrown.getMessage());
    }

    private static VectorSearchService.SearchResult searchResult(String id, String content, float score) {
        VectorSearchService.SearchResult result = new VectorSearchService.SearchResult();
        result.setId(id);
        result.setContent(content);
        result.setScore(score);
        return result;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
