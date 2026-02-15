package com.tradebridge.backend.parse.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.parse.model.OcrExtraction;
import com.tradebridge.backend.parse.model.ParseContext;
import com.tradebridge.backend.parse.service.impl.OpenAiVisionOcrClient;

@Service
public class DocumentOcrService {

    private final OpenAiVisionOcrClient openAiVisionOcrClient;

    public DocumentOcrService(OpenAiVisionOcrClient openAiVisionOcrClient) {
        this.openAiVisionOcrClient = openAiVisionOcrClient;
    }

    public OcrExtraction extract(ParseContext context) {
        Path path = Path.of(context.storagePath());
        String contentType = context.contentType() == null ? "" : context.contentType().toLowerCase();
        String fileName = context.sourceFileName() == null ? "" : context.sourceFileName().toLowerCase();

        if (contentType.contains("pdf") || fileName.endsWith(".pdf")) {
            return extractPdf(path);
        }
        if (contentType.startsWith("image/")) {
            return extractImage(path, context.contentType());
        }
        if (contentType.startsWith("text/") || fileName.endsWith(".txt") || fileName.endsWith(".csv")) {
            return extractTextFile(path);
        }

        return new OcrExtraction("", "none", true, "Unsupported content type for OCR");
    }

    private OcrExtraction extractPdf(Path path) {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            boolean lowConfidence = text == null || text.isBlank();
            return new OcrExtraction(text == null ? "" : text, "pdfbox", lowConfidence, null);
        } catch (Exception ex) {
            return new OcrExtraction("", "pdfbox", true, ex.getMessage());
        }
    }

    private OcrExtraction extractImage(Path path, String contentType) {
        String text = openAiVisionOcrClient.extractImageText(path, contentType);
        boolean lowConfidence = text.isBlank();
        String error = lowConfidence ? "Vision OCR returned empty text (feature disabled or no signal)" : null;
        return new OcrExtraction(text, "openai-vision", lowConfidence, error);
    }

    private OcrExtraction extractTextFile(Path path) {
        try {
            String text = Files.readString(path);
            boolean lowConfidence = text.isBlank();
            return new OcrExtraction(text, "plain-text", lowConfidence, null);
        } catch (IOException ex) {
            return new OcrExtraction("", "plain-text", true, ex.getMessage());
        }
    }
}
