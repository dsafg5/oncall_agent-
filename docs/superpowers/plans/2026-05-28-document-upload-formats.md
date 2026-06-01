# Multi-Format Document Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend knowledge-base uploads from TXT/Markdown to TXT, Markdown, PDF, DOC, and DOCX with type-aware text extraction and chunking.

**Architecture:** Keep the upload controller responsible for validation and saving files. Add a document extraction layer that turns supported file types into plain text plus document type metadata, then let `DocumentChunkService` select chunking behavior by type before `VectorIndexService` generates embeddings and writes to Milvus.

**Tech Stack:** Spring Boot 3.2, Java 17, Milvus Java SDK, Apache PDFBox, Apache POI, JUnit 5.

---

### Task 1: File Type Model and Extraction

**Files:**
- Create: `src/main/java/org/example/dto/DocumentType.java`
- Create: `src/main/java/org/example/dto/DocumentExtractionResult.java`
- Create: `src/main/java/org/example/service/DocumentTextExtractor.java`
- Test: `src/test/java/org/example/service/DocumentTextExtractorTest.java`

- [ ] Write failing tests for extension recognition and TXT extraction.
- [ ] Implement `DocumentType` for `txt`, `md`, `markdown`, `pdf`, `doc`, and `docx`.
- [ ] Implement `DocumentTextExtractor.extract(Path)` using `Files.readString` for TXT/Markdown, PDFBox for PDF, POI XWPF for DOCX, and POI HWPF for DOC.

### Task 2: Type-Aware Chunking

**Files:**
- Modify: `src/main/java/org/example/service/DocumentChunkService.java`
- Test: `src/test/java/org/example/service/DocumentChunkServiceTest.java`

- [ ] Write failing tests that Markdown uses heading-aware splitting and PDF text can split by page boundaries.
- [ ] Add overload `chunkDocument(String content, String filePath, DocumentType documentType)`.
- [ ] Keep existing `chunkDocument(String, String)` as a compatibility wrapper.
- [ ] Implement TXT/DOC/DOCX paragraph chunking, Markdown heading chunking, and PDF page-aware chunking using form-feed page separators from extraction.

### Task 3: Wire Upload Indexing

**Files:**
- Modify: `src/main/java/org/example/service/VectorIndexService.java`
- Modify: `src/main/java/org/example/controller/FileUploadController.java`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/static/app.js`
- Modify: `src/main/resources/static/index.html`

- [ ] Use `DocumentTextExtractor` in `VectorIndexService.indexSingleFile`.
- [ ] Store `_document_type` in Milvus metadata.
- [ ] Allow `txt,md,markdown,pdf,doc,docx` in backend and frontend validation.
- [ ] Ensure unsupported extensions fail before indexing.

### Task 4: Documentation and Verification

**Files:**
- Create: `docs/document-upload-formats.md`
- Modify: `CHANGELOG.md`

- [ ] Document the implementation idea, data flow, supported types, and how each file type is chunked.
- [ ] Run `mvn test`.
- [ ] Review `git diff` to confirm scope.
