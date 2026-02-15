package com.tradebridge.backend.product.service;

import com.tradebridge.backend.parse.model.ParseResult;
import com.tradebridge.backend.product.model.ParseDraftData;

public interface DraftParseWorkflowService {

    ParseDraftData loadForParsing(String draftId);

    void markParsing(String draftId);

    void applyParseResult(String draftId, ParseResult result);

    void markFailed(String draftId, String error);
}
