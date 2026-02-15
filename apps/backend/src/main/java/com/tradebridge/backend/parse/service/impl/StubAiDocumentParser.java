package com.tradebridge.backend.parse.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tradebridge.backend.category.model.CategoryAttributeDefinition;
import com.tradebridge.backend.category.model.CategoryResponse;
import com.tradebridge.backend.category.service.CategoryApplicationService;
import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.parse.model.OcrExtraction;
import com.tradebridge.backend.parse.model.ParseContext;
import com.tradebridge.backend.parse.model.ParseResult;
import com.tradebridge.backend.parse.model.StructuredExtractionResult;
import com.tradebridge.backend.parse.service.AiDocumentParser;
import com.tradebridge.backend.parse.service.DocumentOcrService;

@Component
public class StubAiDocumentParser implements AiDocumentParser {

    private static final Pattern KG_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*kg", Pattern.CASE_INSENSITIVE);

    private final DocumentOcrService documentOcrService;
    private final CategoryApplicationService categoryService;
    private final OpenAiStructuredExtractionClient extractionClient;

    public StubAiDocumentParser(
            DocumentOcrService documentOcrService,
            CategoryApplicationService categoryService,
            OpenAiStructuredExtractionClient extractionClient) {
        this.documentOcrService = documentOcrService;
        this.categoryService = categoryService;
        this.extractionClient = extractionClient;
    }

    @Override
    public ParseResult parse(ParseContext context) {
        OcrExtraction extraction = documentOcrService.extract(context);
        String sourceFileName = context.sourceFileName() == null ? "unknown" : context.sourceFileName();
        String normalizedText = normalize(extraction.text());

        CategoryResponse category = loadCategorySafely(context.categoryId());
        List<CategoryAttributeDefinition> attributes = category == null ? List.of() : category.attributes();

        Map<String, String> fields = new HashMap<>();
        Map<String, Double> confidence = new HashMap<>();

        // Heuristic baseline from OCR and file metadata.
        applyHeuristicBaseline(fields, confidence, sourceFileName, normalizedText, context.contentType(), extraction);

        // Model-based structured extraction overlays heuristic values for known category keys.
        String categoryName = category == null ? "unknown" : category.name();
        StructuredExtractionResult modelResult = extractionClient.extract(
                categoryName,
                sourceFileName,
                normalizedText,
                attributes);

        for (Map.Entry<String, String> entry : modelResult.fields().entrySet()) {
            fields.put(entry.getKey(), entry.getValue());
            confidence.put(entry.getKey(), modelResult.confidence().getOrDefault(entry.getKey(), 0.78));
        }

        fields.put("ham_veri_kaynagi", modelResult.usedModel() ? "ai_parse_openai_structured_v1" : "ai_parse_heuristic_v2");
        fields.put("ocr_engine", extraction.engine());
        confidence.put("ham_veri_kaynagi", 0.99);
        confidence.put("ocr_engine", extraction.lowConfidence() ? 0.6 : 0.95);

        boolean missingRequired = hasMissingRequired(attributes, fields);
        boolean lowConfidence = confidence.values().stream().anyMatch(score -> score < 0.75);
        boolean reviewRequired = extraction.lowConfidence() || missingRequired || lowConfidence;

        String parserName = modelResult.usedModel() ? "openai-structured-v1" : "heuristic-ocr-v2";
        return new ParseResult(Map.copyOf(fields), Map.copyOf(confidence), reviewRequired, parserName);
    }

    private void applyHeuristicBaseline(
            Map<String, String> fields,
            Map<String, Double> confidence,
            String sourceFileName,
            String ocrText,
            String contentType,
            OcrExtraction extraction) {
        String title = inferTitle(sourceFileName, ocrText);
        fields.put("urun_adi", title);

        double titleConfidence = inferTitleConfidence(contentType, extraction.lowConfidence());
        confidence.put("urun_adi", titleConfidence);

        String kgSource = (sourceFileName + " " + ocrText).toLowerCase(Locale.ROOT);
        Matcher kgMatcher = KG_PATTERN.matcher(kgSource);
        if (kgMatcher.find()) {
            String value = kgMatcher.group(1).replace(',', '.');
            fields.put("agirlik_kg", value);
            confidence.put("agirlik_kg", extraction.lowConfidence() ? 0.68 : 0.83);
        }
    }

    private boolean hasMissingRequired(List<CategoryAttributeDefinition> attributes, Map<String, String> fields) {
        for (CategoryAttributeDefinition attr : attributes) {
            if (!Boolean.TRUE.equals(attr.required())) {
                continue;
            }
            String value = fields.get(attr.key());
            if (value == null || value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private CategoryResponse loadCategorySafely(String categoryId) {
        try {
            return categoryService.getById(categoryId);
        } catch (ApiException ex) {
            return null;
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\r', ' ').trim();
    }

    private String inferTitle(String sourceFileName, String ocrText) {
        if (!ocrText.isBlank()) {
            String[] lines = ocrText.split("\\n");
            for (String line : lines) {
                String clean = line.trim();
                if (!clean.isBlank() && clean.length() >= 3) {
                    return clean.length() > 80 ? clean.substring(0, 80) : clean;
                }
            }
        }

        String fallback = sourceFileName.replaceAll("\\.[^.]+$", "").trim();
        return fallback.isBlank() ? "urun" : fallback;
    }

    private double inferTitleConfidence(String contentType, boolean lowOcrConfidence) {
        if (lowOcrConfidence) {
            return 0.62;
        }
        if (contentType == null) {
            return 0.8;
        }
        if (contentType.startsWith("image/")) {
            return 0.76;
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return 0.9;
        }
        return 0.82;
    }
}
