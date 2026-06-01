package org.example.dto;

import lombok.Getter;

@Getter
public class DocumentExtractionResult {

    private final DocumentType documentType;
    private final String content;

    public DocumentExtractionResult(DocumentType documentType, String content) {
        this.documentType = documentType;
        this.content = content == null ? "" : content;
    }
}
