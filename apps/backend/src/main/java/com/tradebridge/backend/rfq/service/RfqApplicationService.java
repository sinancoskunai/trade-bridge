package com.tradebridge.backend.rfq.service;

import com.tradebridge.backend.auth.model.AuthenticatedUser;
import com.tradebridge.backend.rfq.model.BrokerInterventionResponse;
import com.tradebridge.backend.rfq.model.OfferRequest;
import com.tradebridge.backend.rfq.model.OfferResponse;
import com.tradebridge.backend.rfq.model.RfqRequest;
import com.tradebridge.backend.rfq.model.RfqResponse;

public interface RfqApplicationService {

    RfqResponse createRfq(AuthenticatedUser buyer, RfqRequest request);

    OfferResponse createOffer(AuthenticatedUser seller, String rfqId, OfferRequest request);

    OfferResponse counterOffer(AuthenticatedUser buyer, String offerId, OfferRequest request);

    OfferResponse accept(AuthenticatedUser buyer, String offerId);

    OfferResponse reject(AuthenticatedUser buyer, String offerId);

    BrokerInterventionResponse intervene(AuthenticatedUser broker, String rfqId, String note);
}
