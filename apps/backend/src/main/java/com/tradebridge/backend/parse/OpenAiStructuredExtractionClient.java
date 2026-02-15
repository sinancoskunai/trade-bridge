package com.tradebridge.backend.parse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tradebridge.backend.category.CategoryAttributeDefinition;

@Component
public class OpenAiStructuredExtractionClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public OpenAiStructuredExtractionClient(
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.api-key:test-key}") String apiKey,
            @Value("${app.parse.extract.openai-model:gpt-4o-mini}") String model,
            @Value("${app.parse.extract.openai-enabled:false}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
    }

    public StructuredExtractionResult extract(
            String categoryName,
            String sourceFileName,
            String ocrText,
            List<CategoryAttributeDefinition> attributes) {

        if (!enabled || apiKey == null || apiKey.isBlank() || "test-key".equals(apiKey)) {
            return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
        }

        Set<String> allowedKeys = attributes.stream().map(CategoryAttributeDefinition::key)
                .collect(java.util.stream.Collectors.toSet());
        if (allowedKeys.isEmpty()) {
            return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", model);
            payload.put("temperature", 0);
            payload.set("response_format", objectMapper.createObjectNode().put("type", "json_object"));

            ArrayNode messages = payload.putArray("messages");
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content",
                    "Extract product attributes as JSON. Return only JSON object with keys: fields (object), confidence (object). Confidence is 0..1.");

            ObjectNode user = messages.addObject();
            user.put("role", "user");
            user.put("content", buildUserPrompt(categoryName, sourceFileName, ocrText, attributes));

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = readContent(root.path("choices").path(0).path("message").path("content"));
            if (content.isBlank()) {
                return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
            }

            JsonNode parsed = objectMapper.readTree(content);
            JsonNode fieldsNode = parsed.path("fields");
            JsonNode confidenceNode = parsed.path("confidence");

            Map<String, String> fields = sanitizeFields(fieldsNode, allowedKeys);
            Map<String, Double> confidence = sanitizeConfidence(confidenceNode, allowedKeys);

            if (fields.isEmpty()) {
                // backward compatibility if model returned flat object
                fields = sanitizeFields(parsed, allowedKeys);
            }

            return new StructuredExtractionResult(Map.copyOf(fields), Map.copyOf(confidence), !fields.isEmpty(), model);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
        } catch (IOException ex) {
            return new StructuredExtractionResult(Map.of(), Map.of(), false, model);
        }
    }

    private String buildUserPrompt(
            String categoryName,
            String sourceFileName,
            String ocrText,
            List<CategoryAttributeDefinition> attributes) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: ").append(categoryName).append("\n");
        sb.append("Source file: ").append(sourceFileName).append("\n");
        sb.append("Allowed attributes (key,type,required,unit,enumValues):\n");

        for (CategoryAttributeDefinition attr : attributes) {
            sb.append("- ").append(attr.key())
                    .append(", type=").append(attr.type())
                    .append(", required=").append(Boolean.TRUE.equals(attr.required()))
                    .append(", unit=").append(attr.unit() == null ? "" : attr.unit())
                    .append(", enumValues=").append(attr.enumValues() == null ? "" : String.join(",", attr.enumValues()))
                    .append("\n");
        }

        sb.append("\nReturn JSON only in this shape:\n");
        sb.append("{\"fields\":{\"key\":\"value\"},\"confidence\":{\"key\":0.0}}\n");
        sb.append("Include only allowed keys. Use string values for fields.\n");
        sb.append("OCR text:\n");

        String text = ocrText == null ? "" : ocrText;
        if (text.length() > 8000) {
            text = text.substring(0, 8000);
        }
        sb.append(text);
        return sb.toString();
    }

    private String readContent(JsonNode contentNode) {
        if (contentNode.isTextual()) {
            return contentNode.asText("");
        }
        if (contentNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : contentNode) {
                String text = item.path("text").asText("");
                if (!text.isBlank()) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(text);
                }
            }
            return sb.toString();
        }
        return "";
    }

    private Map<String, String> sanitizeFields(JsonNode fieldsNode, Set<String> allowedKeys) {
        if (!fieldsNode.isObject()) {
            return Map.of();
        }

        Map<String, String> out = new HashMap<>();
        Map<String, Object> valueMap = objectMapper.convertValue(fieldsNode, MAP_TYPE);
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            if (!allowedKeys.contains(key)) {
                continue;
            }
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            if (!value.isBlank()) {
                out.put(key, value);
            }
        }
        return out;
    }

    private Map<String, Double> sanitizeConfidence(JsonNode confidenceNode, Set<String> allowedKeys) {
        if (!confidenceNode.isObject()) {
            return Map.of();
        }

        Map<String, Double> out = new HashMap<>();
        Map<String, Object> valueMap = objectMapper.convertValue(confidenceNode, MAP_TYPE);
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            if (!allowedKeys.contains(key)) {
                continue;
            }
            double value = parseDouble(entry.getValue());
            if (value >= 0) {
                out.put(key, Math.min(1.0, value));
            }
        }
        return out;
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return -1;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).replace(',', '.').toLowerCase(Locale.ROOT));
        } catch (Exception ignored) {
            return -1;
        }
    }
}
