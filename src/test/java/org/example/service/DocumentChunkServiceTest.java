package org.example.service;

import org.example.config.DocumentChunkConfig;
import org.example.dto.DocumentChunk;
import org.example.dto.DocumentType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentChunkServiceTest {

    @Test
    void markdownUsesHeadingTitlesForChunks() throws Exception {
        DocumentChunkService service = chunkService(200, 0);

        List<DocumentChunk> chunks = service.chunkDocument(
                "# CPU告警\n立即检查负载\n\n# 缓存告警\n检查缓存命中率",
                "runbook.md",
                DocumentType.MARKDOWN
        );

        assertEquals(2, chunks.size());
        assertEquals("CPU告警", chunks.get(0).getTitle());
        assertEquals("缓存告警", chunks.get(1).getTitle());
    }

    @Test
    void pdfUsesPageBoundariesAsChunkTitles() throws Exception {
        DocumentChunkService service = chunkService(200, 0);

        List<DocumentChunk> chunks = service.chunkDocument(
                "第一页内容\f第二页内容",
                "runbook.pdf",
                DocumentType.PDF
        );

        assertEquals(2, chunks.size());
        assertEquals("page-1", chunks.get(0).getTitle());
        assertEquals("page-2", chunks.get(1).getTitle());
        assertTrue(chunks.get(1).getContent().contains("第二页内容"));
    }

    private static DocumentChunkService chunkService(int maxSize, int overlap) throws Exception {
        DocumentChunkConfig config = new DocumentChunkConfig();
        config.setMaxSize(maxSize);
        config.setOverlap(overlap);

        DocumentChunkService service = new DocumentChunkService();
        Field field = DocumentChunkService.class.getDeclaredField("chunkConfig");
        field.setAccessible(true);
        field.set(service, config);
        return service;
    }
}
