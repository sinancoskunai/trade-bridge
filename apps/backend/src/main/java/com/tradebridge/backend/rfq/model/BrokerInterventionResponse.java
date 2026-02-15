package com.tradebridge.backend.rfq;

public record BrokerInterventionResponse(String rfqId, String brokerUserId, String note, long createdAtEpochMs) {
}
