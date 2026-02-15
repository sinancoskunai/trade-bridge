package com.tradebridge.backend.parse.model;

public record ParseContext(
        String categoryId,
        String sourceFileName,
        String contentType,
        String storagePath) {
}
