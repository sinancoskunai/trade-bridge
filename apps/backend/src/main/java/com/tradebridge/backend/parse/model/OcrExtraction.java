package com.tradebridge.backend.parse.model;

public record OcrExtraction(
        String text,
        String engine,
        boolean lowConfidence,
        String error) {
}
