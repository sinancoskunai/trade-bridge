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

    @Override
    public ParseResult parse(ParseContext context) {
        Map<String, String> fields = new HashMap<>();
        Map<String, Double> confidence = new HashMap<>();

        String source = context.sourceFileName() == null ? "unknown" : context.sourceFileName();
        String title = source.replaceAll("\\.[^.]+$", "").trim();
        if (title.isBlank()) {
            title = "urun";
        }

        fields.put("urun_adi", title);
        fields.put("ham_veri_kaynagi", "ai_parse_stub_v1");

        double titleConfidence = inferTitleConfidence(context.contentType());
        confidence.put("urun_adi", titleConfidence);
        confidence.put("ham_veri_kaynagi", 0.99);

        Matcher kgMatcher = KG_PATTERN.matcher(source.toLowerCase(Locale.ROOT));
        if (kgMatcher.find()) {
            String value = kgMatcher.group(1).replace(',', '.');
            fields.put("agirlik_kg", value);
            confidence.put("agirlik_kg", 0.71);
        }

        boolean reviewRequired = confidence.values().stream().anyMatch(score -> score < 0.75);
        return new ParseResult(Map.copyOf(fields), Map.copyOf(confidence), reviewRequired, "stub-ai-v1");
    }

    private double inferTitleConfidence(String contentType) {
        if (contentType == null) {
            return 0.8;
        }
        if (contentType.startsWith("image/")) {
            return 0.62;
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return 0.88;
        }
        return 0.8;
    }
}
