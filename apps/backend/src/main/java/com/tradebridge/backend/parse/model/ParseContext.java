package com.tradebridge.backend.parse;

public record ParseContext(
        String categoryId,
        String sourceFileName,
        String contentType,
        String storagePath) {
}
