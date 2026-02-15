package com.tradebridge.backend.product;

public record ParseDraftData(
        String draftId,
        String sellerUserId,
        String categoryId,
        String sourceFileName,
        String contentType,
        String storagePath) {
}
