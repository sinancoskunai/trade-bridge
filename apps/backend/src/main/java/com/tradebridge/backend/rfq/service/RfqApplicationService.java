package com.tradebridge.backend.rfq;

import com.tradebridge.backend.auth.AuthenticatedUser;

public interface RfqApplicationService {

    RfqResponse createRfq(AuthenticatedUser buyer, RfqRequest request);

    OfferResponse createOffer(AuthenticatedUser seller, String rfqId, OfferRequest request);

    OfferResponse counterOffer(AuthenticatedUser buyer, String offerId, OfferRequest request);

    OfferResponse accept(AuthenticatedUser buyer, String offerId);

    OfferResponse reject(AuthenticatedUser buyer, String offerId);

    BrokerInterventionResponse intervene(AuthenticatedUser broker, String rfqId, String note);
}
