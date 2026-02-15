package com.tradebridge.backend.rfq.model;

public record RfqResponse(String rfqId, String buyerUserId, String categoryId, String requirementText, String status) {
}
