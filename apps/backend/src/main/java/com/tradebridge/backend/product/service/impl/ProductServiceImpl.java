package com.tradebridge.backend.product.service.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tradebridge.backend.auth.AuthenticatedUser;
import com.tradebridge.backend.category.persistence.entity.CategoryAttributeEntity;
import com.tradebridge.backend.category.persistence.repository.CategoryAttributeRepository;
import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.common.UserRole;
import com.tradebridge.backend.parse.DraftStatuses;
import com.tradebridge.backend.parse.ParseJobApplicationService;
import com.tradebridge.backend.product.model.ProductDraftResponse;
import com.tradebridge.backend.product.model.ProductResponse;
import com.tradebridge.backend.product.model.UpdateDraftRequest;
import com.tradebridge.backend.product.persistence.entity.DocumentEntity;
import com.tradebridge.backend.product.persistence.repository.DocumentRepository;
import com.tradebridge.backend.product.persistence.entity.ProductDraftEntity;
import com.tradebridge.backend.product.persistence.repository.ProductDraftRepository;
import com.tradebridge.backend.product.persistence.entity.ProductEntity;
import com.tradebridge.backend.product.persistence.repository.ProductRepository;
import com.tradebridge.backend.product.service.ProductService;
import com.tradebridge.backend.storage.DocumentStorageService;
import com.tradebridge.backend.storage.StoredFile;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDraftRepository productDraftRepository;
    private final ProductRepository productRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStorageService documentStorageService;
    private final ParseJobApplicationService parseJobService;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final JsonMapCodec jsonMapCodec;

    public ProductServiceImpl(
            ProductDraftRepository productDraftRepository,
            ProductRepository productRepository,
            DocumentRepository documentRepository,
            DocumentStorageService documentStorageService,
            ParseJobApplicationService parseJobService,
            CategoryAttributeRepository categoryAttributeRepository,
            JsonMapCodec jsonMapCodec) {
        this.productDraftRepository = productDraftRepository;
        this.productRepository = productRepository;
        this.documentRepository = documentRepository;
        this.documentStorageService = documentStorageService;
        this.parseJobService = parseJobService;
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.jsonMapCodec = jsonMapCodec;
    }

    @Override
    public ProductDraftResponse createDraft(AuthenticatedUser user, String categoryId, MultipartFile file) {
        if (user.role() != UserRole.SELLER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only SELLER can upload drafts");
        }
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload file is empty");
        }

        ProductDraftEntity draft = new ProductDraftEntity();
        draft.setId(UUID.randomUUID().toString());
        draft.setCategoryId(categoryId);
        draft.setSellerUserId(user.userId());
        draft.setSellerCompanyId(user.companyId());
        draft.setSourceFileName(file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());
        draft.setStatus(DraftStatuses.PENDING_PARSE);
        draft.setParsedFieldsJson(jsonMapCodec.writeStringMap(Map.of()));
        draft.setConfidenceJson(jsonMapCodec.writeDoubleMap(Map.of()));
        draft.setCreatedAt(Instant.now());
        draft.setUpdatedAt(Instant.now());
        draft.setLastError(null);
        productDraftRepository.save(draft);

        StoredFile storedFile;
        try {
            storedFile = documentStorageService.store(draft.getId(), file);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store upload file");
        }

        DocumentEntity document = new DocumentEntity();
        document.setId(UUID.randomUUID().toString());
        document.setDraftId(draft.getId());
        document.setFileName(draft.getSourceFileName());
        document.setContentType(storedFile.contentType());
        document.setFileSize(storedFile.fileSize());
        document.setStoragePath(storedFile.storagePath());
        document.setCreatedAt(Instant.now());
        documentRepository.save(document);

        String parseJobId = parseJobService.createJob(draft.getId());
        draft.setParseJobId(parseJobId);
        draft.setUpdatedAt(Instant.now());
        productDraftRepository.save(draft);
        parseJobService.enqueue(parseJobId);

        return toDraftResponse(draft);
    }

    @Override
    public ProductDraftResponse getDraft(String draftId, AuthenticatedUser user) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));
        if (!draft.getSellerUserId().equals(user.userId()) && user.role() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not accessible");
        }
        return toDraftResponse(draft);
    }

    @Override
    public ProductDraftResponse updateDraft(String draftId, UpdateDraftRequest request, AuthenticatedUser user) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));
        if (!draft.getSellerUserId().equals(user.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not editable");
        }

        Map<String, String> parsed = new HashMap<>(request.parsedFields());
        Map<String, Double> confidence = new HashMap<>(jsonMapCodec.readDoubleMap(draft.getConfidenceJson()));

        for (String key : parsed.keySet()) {
            confidence.put(key, 1.0);
        }

        draft.setParsedFieldsJson(jsonMapCodec.writeStringMap(parsed));
        draft.setConfidenceJson(jsonMapCodec.writeDoubleMap(confidence));
        draft.setStatus(DraftStatuses.REVIEWED);
        draft.setUpdatedAt(Instant.now());
        draft.setLastError(null);
        productDraftRepository.save(draft);
        return toDraftResponse(draft);
    }

    @Override
    public ProductResponse confirmDraft(String draftId, AuthenticatedUser user) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));
        if (!draft.getSellerUserId().equals(user.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Draft is not confirmable");
        }

        Map<String, String> parsedFields = jsonMapCodec.readStringMap(draft.getParsedFieldsJson());
        Map<String, Double> confidence = jsonMapCodec.readDoubleMap(draft.getConfidenceJson());

        validateRequiredFields(draft.getCategoryId(), parsedFields);
        long lowConfidence = confidence.values().stream().filter(score -> score < 0.75).count();
        if (lowConfidence > 0 && !DraftStatuses.REVIEWED.equals(draft.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Low confidence fields exist. Review draft before confirming");
        }

        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID().toString());
        product.setCategoryId(draft.getCategoryId());
        product.setSellerCompanyId(draft.getSellerCompanyId());
        product.setAttributesJson(jsonMapCodec.writeStringMap(parsedFields));
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);

        draft.setStatus(DraftStatuses.CONFIRMED);
        draft.setUpdatedAt(Instant.now());
        productDraftRepository.save(draft);

        return toProductResponse(product);
    }

    @Override
    public List<ProductResponse> listProducts(String categoryId, String query) {
        List<ProductEntity> entities;
        if (categoryId == null || categoryId.isBlank()) {
            entities = productRepository.findByActiveTrue();
        } else {
            entities = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        }

        List<ProductResponse> out = new ArrayList<>();
        for (ProductEntity entity : entities) {
            ProductResponse response = toProductResponse(entity);
            if (query != null && !query.isBlank()) {
                String title = response.attributes().getOrDefault("urun_adi", "").toLowerCase();
                if (!title.contains(query.toLowerCase())) {
                    continue;
                }
            }
            out.add(response);
        }
        return out;
    }

    private void validateRequiredFields(String categoryId, Map<String, String> parsedFields) {
        List<CategoryAttributeEntity> attributes = categoryAttributeRepository.findByCategoryIdOrderByAttrKeyAsc(categoryId);
        for (CategoryAttributeEntity attr : attributes) {
            if (!attr.isRequired()) {
                continue;
            }
            String value = parsedFields.get(attr.getAttrKey());
            if (value == null || value.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Required field " + attr.getAttrKey() + " is missing");
            }
        }
    }

    private ProductDraftResponse toDraftResponse(ProductDraftEntity draft) {
        return new ProductDraftResponse(
                draft.getId(),
                draft.getCategoryId(),
                draft.getSellerUserId(),
                draft.getSourceFileName(),
                Map.copyOf(jsonMapCodec.readStringMap(draft.getParsedFieldsJson())),
                Map.copyOf(jsonMapCodec.readDoubleMap(draft.getConfidenceJson())),
                draft.getStatus(),
                draft.getParseJobId(),
                draft.getLastError());
    }

    private ProductResponse toProductResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getCategoryId(),
                entity.getSellerCompanyId(),
                Map.copyOf(jsonMapCodec.readStringMap(entity.getAttributesJson())),
                entity.isActive());
    }
}
