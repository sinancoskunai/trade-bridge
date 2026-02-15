package com.tradebridge.backend.product.service.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.parse.model.DraftStatuses;
import com.tradebridge.backend.parse.model.ParseResult;
import com.tradebridge.backend.product.model.ParseDraftData;
import com.tradebridge.backend.product.persistence.entity.DocumentEntity;
import com.tradebridge.backend.product.persistence.repository.DocumentRepository;
import com.tradebridge.backend.product.persistence.entity.ProductDraftEntity;
import com.tradebridge.backend.product.persistence.repository.ProductDraftRepository;
import com.tradebridge.backend.product.service.DraftParseWorkflowService;

@Service
public class DraftParseWorkflowServiceImpl implements DraftParseWorkflowService {

    private final ProductDraftRepository productDraftRepository;
    private final DocumentRepository documentRepository;
    private final JsonMapCodec jsonMapCodec;

    public DraftParseWorkflowServiceImpl(
            ProductDraftRepository productDraftRepository,
            DocumentRepository documentRepository,
            JsonMapCodec jsonMapCodec) {
        this.productDraftRepository = productDraftRepository;
        this.documentRepository = documentRepository;
        this.jsonMapCodec = jsonMapCodec;
    }

    @Override
    public ParseDraftData loadForParsing(String draftId) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));

        DocumentEntity document = documentRepository.findByDraftId(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found for draft"));

        return new ParseDraftData(
                draft.getId(),
                draft.getSellerUserId(),
                draft.getCategoryId(),
                draft.getSourceFileName(),
                document.getContentType(),
                document.getStoragePath());
    }

    @Override
    public void markParsing(String draftId) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));
        draft.setStatus(DraftStatuses.PARSING);
        draft.setUpdatedAt(Instant.now());
        draft.setLastError(null);
        productDraftRepository.save(draft);
    }

    @Override
    public void applyParseResult(String draftId, ParseResult result) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Draft not found"));

        Map<String, String> parsedFields = new HashMap<>(jsonMapCodec.readStringMap(draft.getParsedFieldsJson()));
        parsedFields.putAll(result.parsedFields());
        Map<String, Double> confidence = new HashMap<>(jsonMapCodec.readDoubleMap(draft.getConfidenceJson()));
        confidence.putAll(result.confidence());

        draft.setParsedFieldsJson(jsonMapCodec.writeStringMap(parsedFields));
        draft.setConfidenceJson(jsonMapCodec.writeDoubleMap(confidence));
        draft.setStatus(result.reviewRequired() ? DraftStatuses.REVIEW_REQUIRED : DraftStatuses.READY);
        draft.setLastError(null);
        draft.setUpdatedAt(Instant.now());
        productDraftRepository.save(draft);
    }

    @Override
    public void markFailed(String draftId, String error) {
        ProductDraftEntity draft = productDraftRepository.findById(draftId).orElse(null);
        if (draft == null) {
            return;
        }
        draft.setStatus(DraftStatuses.FAILED);
        draft.setUpdatedAt(Instant.now());
        draft.setLastError(error);
        productDraftRepository.save(draft);
    }
}
