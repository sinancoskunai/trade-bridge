package com.tradebridge.backend.parse.service;

import com.tradebridge.backend.parse.model.ParseContext;
import com.tradebridge.backend.parse.model.ParseResult;

public interface AiDocumentParser {
    ParseResult parse(ParseContext context);
}
