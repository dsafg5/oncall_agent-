package org.example.dto;

import java.util.Locale;

public enum DocumentType {
    TEXT("txt"),
    MARKDOWN("md", "markdown"),
    PDF("pdf"),
    WORD_DOC("doc"),
    WORD_DOCX("docx");

    private final String[] extensions;

    DocumentType(String... extensions) {
        this.extensions = extensions;
    }

    public static DocumentType fromFilename(String filename) {
        String extension = extensionOf(filename);
        for (DocumentType type : values()) {
            for (String supported : type.extensions) {
                if (supported.equals(extension)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported document type: " + extension);
    }

    public boolean isTextLike() {
        return this == TEXT || this == MARKDOWN;
    }

    private static String extensionOf(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be empty");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new IllegalArgumentException("File extension is required: " + filename);
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
