package com.tradebridge.backend.parse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.notification.NotificationService;
import com.tradebridge.backend.product.JsonMapCodec;
import com.tradebridge.backend.product.persistence.DocumentEntity;
import com.tradebridge.backend.product.persistence.DocumentRepository;
import com.tradebridge.backend.product.persistence.ParseJobEntity;
import com.tradebridge.backend.product.persistence.ParseJobRepository;
import com.tradebridge.backend.product.persistence.ProductDraftEntity;
import com.tradebridge.backend.product.persistence.ProductDraftRepository;

@Service
public class ParseJobRunner {

    private final ParseJobRepository parseJobRepository;
    private final ProductDraftRepository productDraftRepository;
    private final DocumentRepository documentRepository;
    private final AiDocumentParser parser;
    private final JsonMapCodec jsonMapCodec;
    private final NotificationService notificationService;

    public ParseJobRunner(
            ParseJobRepository parseJobRepository,
            ProductDraftRepository productDraftRepository,
            DocumentRepository documentRepository,
            AiDocumentParser parser,
            JsonMapCodec jsonMapCodec,
            NotificationService notificationService) {
        this.parseJobRepository = parseJobRepository;
        this.productDraftRepository = productDraftRepository;
        this.documentRepository = documentRepository;
        this.parser = parser;
        this.jsonMapCodec = jsonMapCodec;
        this.notificationService = notificationService;
    }

    @Async
    public void runAsync(String parseJobId) {
        run(parseJobId);
    }

    public void run(String parseJobId) {
        ParseJobEntity job = parseJobRepository.findById(parseJobId).orElse(null);
        if (job == null) {
            return;
        }

        ProductDraftEntity draft = productDraftRepository.findById(job.getDraftId()).orElse(null);
        if (draft == null) {
            job.setStatus(ParseStatuses.FAILED);
            job.setLastError("Draft not found");
            job.setUpdatedAt(Instant.now());
            job.setFinishedAt(Instant.now());
            parseJobRepository.save(job);
            return;
        }

        try {
            job.setStatus(ParseStatuses.PARSING);
            job.setAttempts(job.getAttempts() + 1);
            job.setStartedAt(Instant.now());
            job.setUpdatedAt(Instant.now());
            job.setLastError(null);
            parseJobRepository.save(job);

            draft.setStatus(DraftStatuses.PARSING);
            draft.setUpdatedAt(Instant.now());
            draft.setLastError(null);
            productDraftRepository.save(draft);

            DocumentEntity document = documentRepository.findByDraftId(draft.getId())
                    .orElseThrow(() -> new IllegalStateException("Document not found for draft"));

            ParseResult result = parser.parse(new ParseContext(
                    draft.getCategoryId(),
                    draft.getSourceFileName(),
                    document.getContentType(),
                    document.getStoragePath()));

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

            job.setStatus(result.reviewRequired() ? ParseStatuses.REVIEW_REQUIRED : ParseStatuses.COMPLETED);
            job.setUpdatedAt(Instant.now());
            job.setFinishedAt(Instant.now());
            job.setLastError(null);
            parseJobRepository.save(job);

            String notificationType = result.reviewRequired() ? "DOC_PARSE_REVIEW_REQUIRED" : "DOC_PARSE_COMPLETED";
            String message = result.reviewRequired()
                    ? "Dokuman parse tamamlandi, wizard onayi gerekli. Draft ID: " + draft.getId()
                    : "Dokuman parse tamamlandi. Draft ID: " + draft.getId();
            notificationService.push(draft.getSellerUserId(), notificationType, message);
        } catch (Exception ex) {
            job.setStatus(ParseStatuses.FAILED);
            job.setUpdatedAt(Instant.now());
            job.setFinishedAt(Instant.now());
            job.setLastError(ex.getMessage());
            parseJobRepository.save(job);

            draft.setStatus(DraftStatuses.FAILED);
            draft.setUpdatedAt(Instant.now());
            draft.setLastError(ex.getMessage());
            productDraftRepository.save(draft);

            notificationService.push(draft.getSellerUserId(), "DOC_PARSE_FAILED",
                    "Dokuman parse hatasi. Draft ID: " + draft.getId());
        }
    }
}
