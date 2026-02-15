package com.tradebridge.backend.storage;

public record StoredFile(String storagePath, long fileSize, String contentType) {
}
