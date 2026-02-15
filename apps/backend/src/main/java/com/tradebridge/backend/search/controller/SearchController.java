package com.tradebridge.backend.search;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/buyer/search")
public class SearchController {

    private final SearchApplicationService searchService;

    public SearchController(SearchApplicationService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/qa")
    public SearchQaResponse qa(@Valid @RequestBody SearchQaRequest request) {
        return searchService.ask(request);
    }
}
