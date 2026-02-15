package com.tradebridge.backend.parse.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.notification.service.NotificationApplicationService;
import com.tradebridge.backend.parse.model.ParseContext;
import com.tradebridge.backend.parse.model.ParseJobData;
import com.tradebridge.backend.parse.model.ParseResult;
import com.tradebridge.backend.product.model.ParseDraftData;
import com.tradebridge.backend.product.service.DraftParseWorkflowService;
import com.tradebridge.backend.parse.service.AiDocumentParser;

@Service
public class ParseJobRunner {

    private final ParseJobStateService parseJobStateService;
    private final DraftParseWorkflowService draftParseWorkflowService;
    private final AiDocumentParser parser;
    private final NotificationApplicationService notificationService;

    public ParseJobRunner(
            ParseJobStateService parseJobStateService,
            DraftParseWorkflowService draftParseWorkflowService,
            AiDocumentParser parser,
            NotificationApplicationService notificationService) {
        this.parseJobStateService = parseJobStateService;
        this.draftParseWorkflowService = draftParseWorkflowService;
        this.parser = parser;
        this.notificationService = notificationService;
    }

    @Async
    public void runAsync(String parseJobId) {
        run(parseJobId);
    }

    public void run(String parseJobId) {
        String draftId = null;
        String sellerUserId = null;

        try {
            ParseJobData job = parseJobStateService.markParsing(parseJobId);
            if (job == null) {
                return;
            }
            draftId = job.draftId();

            draftParseWorkflowService.markParsing(draftId);
            ParseDraftData draft = draftParseWorkflowService.loadForParsing(draftId);
            sellerUserId = draft.sellerUserId();

            ParseResult result = parser.parse(new ParseContext(
                    draft.categoryId(),
                    draft.sourceFileName(),
                    draft.contentType(),
                    draft.storagePath()));

            draftParseWorkflowService.applyParseResult(draftId, result);
            parseJobStateService.markFinished(parseJobId, result.reviewRequired());

            String notificationType = result.reviewRequired() ? "DOC_PARSE_REVIEW_REQUIRED" : "DOC_PARSE_COMPLETED";
            String message = result.reviewRequired()
                    ? "Dokuman parse tamamlandi, wizard onayi gerekli. Draft ID: " + draftId
                    : "Dokuman parse tamamlandi. Draft ID: " + draftId;
            notificationService.push(sellerUserId, notificationType, message);
        } catch (Exception ex) {
            parseJobStateService.markFailed(parseJobId, ex.getMessage());
            if (draftId != null) {
                draftParseWorkflowService.markFailed(draftId, ex.getMessage());
            }
            if (sellerUserId != null) {
                notificationService.push(sellerUserId, "DOC_PARSE_FAILED",
                        "Dokuman parse hatasi. Draft ID: " + draftId);
            }
        }
    }
}
