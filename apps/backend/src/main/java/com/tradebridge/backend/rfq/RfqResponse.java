package com.tradebridge.backend.rfq;

public record RfqResponse(String rfqId, String buyerUserId, String categoryId, String requirementText, String status) {
}
