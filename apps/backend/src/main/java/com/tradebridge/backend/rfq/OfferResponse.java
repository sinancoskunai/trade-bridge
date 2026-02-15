package com.tradebridge.backend.rfq;

public record OfferResponse(String offerId, String rfqId, String sellerUserId, double price, String currency, String note,
        String status, int revision) {
}
