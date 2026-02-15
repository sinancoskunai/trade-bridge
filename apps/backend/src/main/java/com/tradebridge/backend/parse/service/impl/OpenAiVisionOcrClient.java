package com.tradebridge.backend.parse.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class OpenAiVisionOcrClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public OpenAiVisionOcrClient(
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.api-key:test-key}") String apiKey,
            @Value("${app.parse.ocr.openai-model:gpt-4o-mini}") String model,
            @Value("${app.parse.ocr.openai-enabled:false}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
    }

    public String extractImageText(Path filePath, String contentType) {
        if (!enabled || apiKey == null || apiKey.isBlank() || "test-key".equals(apiKey)) {
            return "";
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String safeContentType = (contentType == null || contentType.isBlank()) ? "image/png" : contentType;
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + safeContentType + ";base64," + base64;

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", model);
            payload.put("temperature", 0);

            ArrayNode messages = payload.putArray("messages");
            ObjectNode user = messages.addObject();
            user.put("role", "user");
            ArrayNode content = user.putArray("content");

            ObjectNode textNode = content.addObject();
            textNode.put("type", "text");
            textNode.put("text", "Extract plain text from this image. Return only text without commentary.");

            ObjectNode imageNode = content.addObject();
            imageNode.put("type", "image_url");
            ObjectNode imageUrlNode = imageNode.putObject("image_url");
            imageUrlNode.put("url", dataUrl);

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return "";
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
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
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "";
        } catch (IOException ex) {
            return "";
        }
    }
}
