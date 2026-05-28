package org.example.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.R;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.SearchParam;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VectorSearchServiceTest {

    @Test
    void searchLoadsCollectionBeforeQueryingMilvus() throws Exception {
        MilvusServiceClient milvusClient = mock(MilvusServiceClient.class);
        VectorEmbeddingService embeddingService = mock(VectorEmbeddingService.class);
        VectorSearchService service = new VectorSearchService();
        setField(service, "milvusClient", milvusClient);
        setField(service, "embeddingService", embeddingService);

        when(embeddingService.generateQueryVector(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f));
        when(milvusClient.loadCollection(any(LoadCollectionParam.class))).thenReturn(R.success());
        when(milvusClient.search(any(SearchParam.class))).thenThrow(new RuntimeException("stop after search"));

        assertThrows(RuntimeException.class, () -> service.searchSimilarDocuments("cpu high", 3));

        InOrder inOrder = inOrder(milvusClient);
        inOrder.verify(milvusClient).loadCollection(any(LoadCollectionParam.class));
        inOrder.verify(milvusClient).search(any(SearchParam.class));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
