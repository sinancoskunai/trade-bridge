package com.tradebridge.backend.search.service;

import com.tradebridge.backend.search.model.SearchQaRequest;
import com.tradebridge.backend.search.model.SearchQaResponse;

public interface SearchApplicationService {

    SearchQaResponse ask(SearchQaRequest request);
}
