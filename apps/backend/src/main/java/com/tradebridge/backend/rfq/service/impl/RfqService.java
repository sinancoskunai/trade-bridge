package com.tradebridge.backend.rfq;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.auth.AuthenticatedUser;
import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.common.UserRole;
import com.tradebridge.backend.notification.NotificationApplicationService;

@Service
public class RfqService implements RfqApplicationService {

    private final Map<String, RfqMutable> rfqs = new ConcurrentHashMap<>();
    private final Map<String, OfferMutable> offers = new ConcurrentHashMap<>();
    private final NotificationApplicationService notificationService;

    public RfqService(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public RfqResponse createRfq(AuthenticatedUser buyer, RfqRequest request) {
        if (buyer.role() != UserRole.BUYER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only BUYER can create RFQ");
        }
        String id = UUID.randomUUID().toString();
        RfqMutable rfq = new RfqMutable(id, buyer.userId(), request.categoryId(), request.requirementText(), "OPEN");
        rfqs.put(id, rfq);
        notificationService.push(buyer.userId(), "RFQ_CREATED", "RFQ olusturuldu: " + id);
        return rfq.toResponse();
    }

    @Override
    public OfferResponse createOffer(AuthenticatedUser seller, String rfqId, OfferRequest request) {
        if (seller.role() != UserRole.SELLER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only SELLER can offer");
        }
        RfqMutable rfq = mustRfq(rfqId);
        if (!"OPEN".equals(rfq.status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RFQ is not open");
        }

        String offerId = UUID.randomUUID().toString();
        OfferMutable offer = new OfferMutable(offerId, rfqId, seller.userId(), request.price(), request.currency(),
                request.note(), "PENDING_BUYER", 1);
        offers.put(offerId, offer);
        notificationService.push(rfq.buyerUserId, "OFFER_RECEIVED", "Yeni teklif geldi: " + offerId);
        return offer.toResponse();
    }

    @Override
    public OfferResponse counterOffer(AuthenticatedUser buyer, String offerId, OfferRequest request) {
        OfferMutable offer = mustOffer(offerId);
        RfqMutable rfq = mustRfq(offer.rfqId);
        if (!rfq.buyerUserId.equals(buyer.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Offer not accessible by buyer");
        }
        if ("ACCEPTED".equals(offer.status) || "REJECTED".equals(offer.status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Finalized offer cannot be revised");
        }
        offer.price = request.price();
        offer.currency = request.currency();
        offer.note = request.note();
        offer.revision += 1;
        offer.status = "COUNTERED";
        notificationService.push(offer.sellerUserId, "COUNTER_RECEIVED", "Karsi teklif geldi: " + offerId);
        return offer.toResponse();
    }

    @Override
    public OfferResponse accept(AuthenticatedUser buyer, String offerId) {
        OfferMutable offer = mustOffer(offerId);
        RfqMutable rfq = mustRfq(offer.rfqId);
        if (!rfq.buyerUserId.equals(buyer.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Offer not accessible by buyer");
        }
        offer.status = "ACCEPTED";
        rfq.status = "CLOSED";
        notificationService.push(offer.sellerUserId, "OFFER_ACCEPTED", "Teklif kabul edildi: " + offerId);
        return offer.toResponse();
    }

    @Override
    public OfferResponse reject(AuthenticatedUser buyer, String offerId) {
        OfferMutable offer = mustOffer(offerId);
        RfqMutable rfq = mustRfq(offer.rfqId);
        if (!rfq.buyerUserId.equals(buyer.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Offer not accessible by buyer");
        }
        offer.status = "REJECTED";
        notificationService.push(offer.sellerUserId, "OFFER_REJECTED", "Teklif reddedildi: " + offerId);
        return offer.toResponse();
    }

    @Override
    public BrokerInterventionResponse intervene(AuthenticatedUser broker, String rfqId, String note) {
        if (broker.role() != UserRole.BROKER && broker.role() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only BROKER or ADMIN can intervene");
        }
        RfqMutable rfq = mustRfq(rfqId);
        notificationService.push(rfq.buyerUserId, "BROKER_INTERVENTION", note);
        return new BrokerInterventionResponse(rfqId, broker.userId(), note, System.currentTimeMillis());
    }

    private RfqMutable mustRfq(String rfqId) {
        RfqMutable rfq = rfqs.get(rfqId);
        if (rfq == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "RFQ not found");
        }
        return rfq;
    }

    private OfferMutable mustOffer(String offerId) {
        OfferMutable offer = offers.get(offerId);
        if (offer == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Offer not found");
        }
        return offer;
    }

    private static final class RfqMutable {
        private final String rfqId;
        private final String buyerUserId;
        private final String categoryId;
        private final String requirementText;
        private String status;

        private RfqMutable(String rfqId, String buyerUserId, String categoryId, String requirementText, String status) {
            this.rfqId = rfqId;
            this.buyerUserId = buyerUserId;
            this.categoryId = categoryId;
            this.requirementText = requirementText;
            this.status = status;
        }

        RfqResponse toResponse() {
            return new RfqResponse(rfqId, buyerUserId, categoryId, requirementText, status);
        }
    }

    private static final class OfferMutable {
        private final String offerId;
        private final String rfqId;
        private final String sellerUserId;
        private double price;
        private String currency;
        private String note;
        private String status;
        private int revision;

        private OfferMutable(String offerId, String rfqId, String sellerUserId, double price, String currency, String note,
                String status, int revision) {
            this.offerId = offerId;
            this.rfqId = rfqId;
            this.sellerUserId = sellerUserId;
            this.price = price;
            this.currency = currency;
            this.note = note;
            this.status = status;
            this.revision = revision;
        }

        OfferResponse toResponse() {
            return new OfferResponse(offerId, rfqId, sellerUserId, price, currency, note, status, revision);
        }
    }
}
