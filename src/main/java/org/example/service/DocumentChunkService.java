package org.example.service;

import org.example.config.DocumentChunkConfig;
import org.example.dto.DocumentChunk;
import org.example.dto.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentChunkService {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    @Autowired
    private DocumentChunkConfig chunkConfig;

    public List<DocumentChunk> chunkDocument(String content, String filePath) {
        DocumentType documentType;
        try {
            documentType = DocumentType.fromFilename(filePath);
        } catch (IllegalArgumentException e) {
            documentType = DocumentType.TEXT;
        }
        return chunkDocument(content, filePath, documentType);
    }

    public List<DocumentChunk> chunkDocument(String content, String filePath, DocumentType documentType) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }

        List<Section> sections = switch (documentType) {
            case MARKDOWN -> splitMarkdownSections(content);
            case PDF -> splitPdfPages(content);
            case TEXT, WORD_DOC, WORD_DOCX -> List.of(new Section(null, content.trim(), 0));
        };

        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (Section section : sections) {
            List<DocumentChunk> sectionChunks = chunkSection(section, chunkIndex);
            chunks.addAll(sectionChunks);
            chunkIndex += sectionChunks.size();
        }
        return chunks;
    }

    private List<Section> splitMarkdownSections(String content) {
        List<Section> sections = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(content);
        String currentTitle = null;
        int currentContentStart = 0;

        while (matcher.find()) {
            if (currentTitle != null && currentContentStart < matcher.start()) {
                addSection(sections, currentTitle, content.substring(currentContentStart, matcher.start()), currentContentStart);
            } else if (currentTitle == null && currentContentStart < matcher.start()) {
                addSection(sections, null, content.substring(currentContentStart, matcher.start()), currentContentStart);
            }
            currentTitle = matcher.group(2).trim();
            currentContentStart = matcher.end();
        }

        if (currentContentStart < content.length()) {
            addSection(sections, currentTitle, content.substring(currentContentStart), currentContentStart);
        }

        if (sections.isEmpty()) {
            sections.add(new Section(null, content.trim(), 0));
        }
        return sections;
    }

    private List<Section> splitPdfPages(String content) {
        List<Section> sections = new ArrayList<>();
        String[] pages = content.split("\\f", -1);
        int offset = 0;
        for (int i = 0; i < pages.length; i++) {
            String page = pages[i];
            if (!page.trim().isEmpty()) {
                sections.add(new Section("page-" + (i + 1), page.trim(), offset));
            }
            offset += page.length() + 1;
        }
        if (sections.isEmpty()) {
            sections.add(new Section("page-1", content.trim(), 0));
        }
        return sections;
    }

    private void addSection(List<Section> sections, String title, String content, int startIndex) {
        String trimmed = content.trim();
        if (!trimmed.isEmpty()) {
            sections.add(new Section(title, trimmed, startIndex));
        }
    }

    private List<DocumentChunk> chunkSection(Section section, int startChunkIndex) {
        if (section.content.length() <= chunkConfig.getMaxSize()) {
            DocumentChunk chunk = new DocumentChunk(
                    section.content,
                    section.startIndex,
                    section.startIndex + section.content.length(),
                    startChunkIndex
            );
            chunk.setTitle(section.title);
            return List.of(chunk);
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        List<String> paragraphs = splitByParagraphs(section.content);
        StringBuilder currentChunk = new StringBuilder();
        int currentStartIndex = section.startIndex;
        int chunkIndex = startChunkIndex;

        for (String paragraph : paragraphs) {
            if (currentChunk.length() > 0
                    && currentChunk.length() + paragraph.length() > chunkConfig.getMaxSize()) {
                String chunkContent = currentChunk.toString().trim();
                chunks.add(createChunk(section.title, chunkContent, currentStartIndex, chunkIndex++));

                String overlap = getOverlapText(chunkContent);
                currentChunk = new StringBuilder(overlap);
                currentStartIndex = currentStartIndex + chunkContent.length() - overlap.length();
            }
            currentChunk.append(paragraph).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            String chunkContent = currentChunk.toString().trim();
            chunks.add(createChunk(section.title, chunkContent, currentStartIndex, chunkIndex));
        }

        return chunks;
    }

    private DocumentChunk createChunk(String title, String content, int startIndex, int chunkIndex) {
        DocumentChunk chunk = new DocumentChunk(content, startIndex, startIndex + content.length(), chunkIndex);
        chunk.setTitle(title);
        return chunk;
    }

    private List<String> splitByParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        for (String part : content.split("\\R\\s*\\R+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs.add(content.trim());
        }
        return paragraphs;
    }

    private String getOverlapText(String text) {
        int overlapSize = Math.min(chunkConfig.getOverlap(), text.length());
        if (overlapSize <= 0) {
            return "";
        }
        return text.substring(text.length() - overlapSize).trim();
    }

    private record Section(String title, String content, int startIndex) {
    }
}
