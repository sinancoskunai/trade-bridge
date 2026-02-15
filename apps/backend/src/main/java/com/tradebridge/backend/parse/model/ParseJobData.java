package com.tradebridge.backend.parse;

public record ParseJobData(String parseJobId, String draftId, int attempts) {
}
