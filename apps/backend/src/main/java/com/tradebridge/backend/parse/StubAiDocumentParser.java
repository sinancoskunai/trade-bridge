package com.tradebridge.backend.parse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class StubAiDocumentParser implements AiDocumentParser {

    private static final Pattern KG_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*kg", Pattern.CASE_INSENSITIVE);

    private final DocumentOcrService documentOcrService;

    public StubAiDocumentParser(DocumentOcrService documentOcrService) {
        this.documentOcrService = documentOcrService;
    }

    @Override
    public ParseResult parse(ParseContext context) {
        Map<String, String> fields = new HashMap<>();
        Map<String, Double> confidence = new HashMap<>();

        OcrExtraction extraction = documentOcrService.extract(context);
        String sourceFileName = context.sourceFileName() == null ? "unknown" : context.sourceFileName();
        String normalizedText = normalize(extraction.text());

        String title = inferTitle(sourceFileName, normalizedText);
        fields.put("urun_adi", title);
        fields.put("ham_veri_kaynagi", "ai_parse_heuristic_v2");
        fields.put("ocr_engine", extraction.engine());

        double titleConfidence = inferTitleConfidence(context.contentType(), extraction.lowConfidence());
        confidence.put("urun_adi", titleConfidence);
        confidence.put("ham_veri_kaynagi", 0.99);
        confidence.put("ocr_engine", extraction.lowConfidence() ? 0.6 : 0.95);

        String kgSource = (sourceFileName + " " + normalizedText).toLowerCase(Locale.ROOT);
        Matcher kgMatcher = KG_PATTERN.matcher(kgSource);
        if (kgMatcher.find()) {
            String value = kgMatcher.group(1).replace(',', '.');
            fields.put("agirlik_kg", value);
            confidence.put("agirlik_kg", extraction.lowConfidence() ? 0.68 : 0.83);
        }

        boolean reviewRequired = extraction.lowConfidence()
                || confidence.values().stream().anyMatch(score -> score < 0.75);
        return new ParseResult(Map.copyOf(fields), Map.copyOf(confidence), reviewRequired, "heuristic-ocr-v2");
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
