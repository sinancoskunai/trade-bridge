package com.tradebridge.backend.rfq;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.auth.SecurityUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    @PostMapping("/buyer/rfqs")
    public RfqResponse create(@Valid @RequestBody RfqRequest request) {
        return rfqService.createRfq(SecurityUtil.currentUser(), request);
    }

    @PostMapping("/seller/rfqs/{rfqId}/offers")
    public OfferResponse offer(@PathVariable String rfqId, @Valid @RequestBody OfferRequest request) {
        return rfqService.createOffer(SecurityUtil.currentUser(), rfqId, request);
    }

    @PostMapping("/buyer/offers/{offerId}/counter")
    public OfferResponse counter(@PathVariable String offerId, @Valid @RequestBody OfferRequest request) {
        return rfqService.counterOffer(SecurityUtil.currentUser(), offerId, request);
    }

    @PostMapping("/buyer/offers/{offerId}/accept")
    public OfferResponse accept(@PathVariable String offerId) {
        return rfqService.accept(SecurityUtil.currentUser(), offerId);
    }

    @PostMapping("/buyer/offers/{offerId}/reject")
    public OfferResponse reject(@PathVariable String offerId) {
        return rfqService.reject(SecurityUtil.currentUser(), offerId);
    }

    @PostMapping("/broker/rfqs/{rfqId}/interventions")
    public BrokerInterventionResponse intervene(
            @PathVariable String rfqId,
            @Valid @RequestBody BrokerInterventionRequest request) {
        return rfqService.intervene(SecurityUtil.currentUser(), rfqId, request.note());
    }
}
