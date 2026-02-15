package com.tradebridge.backend.parse;

public record ParseJobResponse(
        String parseJobId,
        String draftId,
        String status,
        int attempts,
        String lastError,
        String updatedAt,
        String startedAt,
        String finishedAt) {
}
