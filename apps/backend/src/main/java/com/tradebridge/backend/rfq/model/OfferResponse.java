package com.tradebridge.backend.rfq.model;

public record OfferResponse(String offerId, String rfqId, String sellerUserId, double price, String currency, String note,
        String status, int revision) {
}
