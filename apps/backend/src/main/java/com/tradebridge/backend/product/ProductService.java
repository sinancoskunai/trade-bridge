package com.tradebridge.backend.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tradebridge.backend.auth.AuthenticatedUser;
import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.common.UserRole;
import com.tradebridge.backend.notification.NotificationService;

@Service
public class ProductService {

    private final Map<String, DraftMutable> drafts = new ConcurrentHashMap<>();
    private final Map<String, ProductMutable> products = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public ProductService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ProductDraftResponse createDraft(AuthenticatedUser user, String categoryId, MultipartFile file) {
        if (user.role() != UserRole.SELLER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only SELLER can upload drafts");
        }
        String draftId = UUID.randomUUID().toString();
        DraftMutable draft = new DraftMutable();
        draft.draftId = draftId;
        draft.categoryId = categoryId;
        draft.sellerUserId = user.userId();
        draft.sellerCompanyId = user.companyId();
        draft.sourceFileName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        draft.status = "DRAFT";

        String defaultTitle = draft.sourceFileName.replaceAll("\\.[^.]+$", "");
        draft.parsedFields.put("urun_adi", defaultTitle);
        draft.parsedFields.put("ham_veri_kaynagi", "ai_parse_stub");
        draft.confidence.put("urun_adi", 0.82);
        draft.confidence.put("ham_veri_kaynagi", 0.95);

        drafts.put(draftId, draft);
        notificationService.push(user.userId(), "DOC_PARSE_COMPLETED",
                "Dokuman parse tamamlandi. Draft ID: " + draftId);
        return draft.toResponse();
    }

    public ProductDraftResponse getDraft(String draftId, AuthenticatedUser user) {
        DraftMutable draft = drafts.get(draftId);
        if (draft == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Draft not found");
        }
        if (!draft.sellerUserId.equals(user.userId()) && user.role() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not accessible");
        }
        return draft.toResponse();
    }

    public ProductDraftResponse updateDraft(String draftId, UpdateDraftRequest request, AuthenticatedUser user) {
        DraftMutable draft = drafts.get(draftId);
        if (draft == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Draft not found");
        }
        if (!draft.sellerUserId.equals(user.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not editable");
        }
        draft.parsedFields.clear();
        draft.parsedFields.putAll(request.parsedFields());
        draft.status = "REVIEWED";
        return draft.toResponse();
    }

    public ProductResponse confirmDraft(String draftId, AuthenticatedUser user) {
        DraftMutable draft = drafts.get(draftId);
        if (draft == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Draft not found");
        }
        if (!draft.sellerUserId.equals(user.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not confirmable");
        }
        if (!draft.parsedFields.containsKey("urun_adi") || draft.parsedFields.get("urun_adi").isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Required field urun_adi is missing");
        }

        String productId = UUID.randomUUID().toString();
        ProductMutable product = new ProductMutable();
        product.productId = productId;
        product.categoryId = draft.categoryId;
        product.sellerCompanyId = draft.sellerCompanyId;
        product.attributes.putAll(draft.parsedFields);
        product.active = true;
        products.put(productId, product);
        draft.status = "CONFIRMED";

        return product.toResponse();
    }

    public List<ProductResponse> listProducts(String categoryId, String query) {
        List<ProductResponse> out = new ArrayList<>();
        for (ProductMutable product : products.values()) {
            if (categoryId != null && !categoryId.isBlank() && !categoryId.equals(product.categoryId)) {
                continue;
            }
            if (query != null && !query.isBlank()) {
                String title = product.attributes.getOrDefault("urun_adi", "").toLowerCase();
                if (!title.contains(query.toLowerCase())) {
                    continue;
                }
            }
            out.add(product.toResponse());
        }
        return out;
    }

    private static final class DraftMutable {
        private String draftId;
        private String categoryId;
        private String sellerUserId;
        private String sellerCompanyId;
        private String sourceFileName;
        private final Map<String, String> parsedFields = new HashMap<>();
        private final Map<String, Double> confidence = new HashMap<>();
        private String status;

        ProductDraftResponse toResponse() {
            return new ProductDraftResponse(draftId, categoryId, sellerUserId, sourceFileName, Map.copyOf(parsedFields),
                    Map.copyOf(confidence), status);
        }
    }

    private static final class ProductMutable {
        private String productId;
        private String categoryId;
        private String sellerCompanyId;
        private final Map<String, String> attributes = new HashMap<>();
        private boolean active;

        ProductResponse toResponse() {
            return new ProductResponse(productId, categoryId, sellerCompanyId, Map.copyOf(attributes), active);
        }
    }
}
