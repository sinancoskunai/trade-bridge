package com.tradebridge.backend.rfq.model;

public record BrokerInterventionResponse(String rfqId, String brokerUserId, String note, long createdAtEpochMs) {
}
