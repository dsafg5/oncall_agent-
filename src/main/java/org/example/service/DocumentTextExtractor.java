package org.example.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.example.dto.DocumentExtractionResult;
import org.example.dto.DocumentType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocumentTextExtractor {

    public DocumentExtractionResult extract(Path path) throws IOException {
        DocumentType documentType = DocumentType.fromFilename(path.getFileName().toString());
        String content = switch (documentType) {
            case TEXT, MARKDOWN -> Files.readString(path);
            case PDF -> extractPdf(path);
            case WORD_DOCX -> extractDocx(path);
            case WORD_DOC -> extractDoc(path);
        };
        return new DocumentExtractionResult(documentType, content.trim());
    }

    private String extractPdf(Path path) throws IOException {
        try (PDDocument document = PDDocument.load(path.toFile())) {
            StringBuilder text = new StringBuilder();
            int pageCount = document.getNumberOfPages();
            for (int page = 1; page <= pageCount; page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                if (text.length() > 0) {
                    text.append("\f");
                }
                text.append(stripper.getText(document).trim());
            }
            return text.toString();
        }
    }

    private String extractDocx(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                appendLine(text, paragraph.getText());
            }
            for (XWPFTable table : document.getTables()) {
                appendTable(text, table);
            }
            return text.toString();
        }
    }

    private String extractDoc(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path);
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private void appendTable(StringBuilder text, XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            StringBuilder rowText = new StringBuilder();
            for (XWPFTableCell cell : row.getTableCells()) {
                if (rowText.length() > 0) {
                    rowText.append(" | ");
                }
                rowText.append(cell.getText());
            }
            appendLine(text, rowText.toString());
        }
    }

    private void appendLine(StringBuilder text, String value) {
        if (value != null && !value.isBlank()) {
            text.append(value.trim()).append("\n\n");
        }
    }
}
