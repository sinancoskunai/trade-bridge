package com.tradebridge.backend.parse;

public record OcrExtraction(
        String text,
        String engine,
        boolean lowConfidence,
        String error) {
}
