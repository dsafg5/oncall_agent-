package org.example.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.dto.DocumentExtractionResult;
import org.example.dto.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTextExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsSupportedDocumentTypesFromExtensions() {
        assertEquals(DocumentType.TEXT, DocumentType.fromFilename("runbook.txt"));
        assertEquals(DocumentType.MARKDOWN, DocumentType.fromFilename("runbook.md"));
        assertEquals(DocumentType.MARKDOWN, DocumentType.fromFilename("runbook.markdown"));
        assertEquals(DocumentType.PDF, DocumentType.fromFilename("runbook.pdf"));
        assertEquals(DocumentType.WORD_DOC, DocumentType.fromFilename("runbook.doc"));
        assertEquals(DocumentType.WORD_DOCX, DocumentType.fromFilename("runbook.docx"));
    }

    @Test
    void rejectsUnsupportedExtensions() {
        assertThrows(IllegalArgumentException.class,
                () -> DocumentType.fromFilename("runbook.xlsx"));
    }

    @Test
    void extractsPlainTextFiles() throws Exception {
        Path textFile = tempDir.resolve("note.txt");
        Files.writeString(textFile, "question: 11\nanswer: 22");

        DocumentTextExtractor extractor = new DocumentTextExtractor();
        DocumentExtractionResult result = extractor.extract(textFile);

        assertEquals(DocumentType.TEXT, result.getDocumentType());
        assertTrue(result.getContent().contains("answer: 22"));
    }

    @Test
    void extractsPdfFilesWithFormFeedPageSeparators() throws Exception {
        Path pdfFile = tempDir.resolve("runbook.pdf");
        try (PDDocument document = new PDDocument()) {
            addPdfPage(document, "first pdf page");
            addPdfPage(document, "second pdf page");
            document.save(pdfFile.toFile());
        }

        DocumentTextExtractor extractor = new DocumentTextExtractor();
        DocumentExtractionResult result = extractor.extract(pdfFile);

        assertEquals(DocumentType.PDF, result.getDocumentType());
        assertTrue(result.getContent().contains("first pdf page"));
        assertTrue(result.getContent().contains("second pdf page"));
        assertTrue(result.getContent().contains("\f"));
    }

    @Test
    void extractsDocxFiles() throws Exception {
        Path docxFile = tempDir.resolve("runbook.docx");
        try (XWPFDocument document = new XWPFDocument();
             OutputStream outputStream = Files.newOutputStream(docxFile)) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("docx incident runbook");
            document.write(outputStream);
        }

        DocumentTextExtractor extractor = new DocumentTextExtractor();
        DocumentExtractionResult result = extractor.extract(docxFile);

        assertEquals(DocumentType.WORD_DOCX, result.getDocumentType());
        assertTrue(result.getContent().contains("docx incident runbook"));
    }

    @Test
    void extractsBinaryDocFiles() throws Exception {
        Path docFile = Path.of(Objects.requireNonNull(
                getClass().getResource("/fixtures/runbook.doc")
        ).toURI());

        DocumentTextExtractor extractor = new DocumentTextExtractor();
        DocumentExtractionResult result = extractor.extract(docFile);

        assertEquals(DocumentType.WORD_DOC, result.getDocumentType());
        assertTrue(result.getContent().contains("simple word document"));
    }

    private void addPdfPage(PDDocument document, String text) throws Exception {
        PDPage page = new PDPage();
        document.addPage(page);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(72, 720);
            contentStream.showText(text);
            contentStream.endText();
        }
    }
}
