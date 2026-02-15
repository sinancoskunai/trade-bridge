package com.tradebridge.backend.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tradebridge.backend.product.model.ProductResponse;
import com.tradebridge.backend.product.service.ProductService;

@Service
public class SearchService {

    private final ProductService productService;

    public SearchService(ProductService productService) {
        this.productService = productService;
    }

    public SearchQaResponse ask(SearchQaRequest request) {
        Map<String, String> interpreted = new HashMap<>();
        interpreted.put("queryText", request.queryText());
        if (request.categoryId() != null && !request.categoryId().isBlank()) {
            interpreted.put("categoryId", request.categoryId());
        }
        if (request.contextFilters() != null) {
            interpreted.putAll(request.contextFilters());
        }

        List<ProductResponse> matches = productService.listProducts(request.categoryId(), request.queryText());
        List<String> followUps = new ArrayList<>();
        if (matches.isEmpty()) {
            followUps.add("Hangi agirlik araligini tercih edersiniz?");
            followUps.add("Belirli bir mensei var mi?");
        }

        return new SearchQaResponse(
                interpreted,
                followUps,
                matches,
                "Sorgu, guvenli FilterDSL stub'u ile yorumlanip filtrelere donusturuldu.");
    }
}
