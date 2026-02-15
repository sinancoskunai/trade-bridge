package com.tradebridge.backend.product.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tradebridge.backend.auth.SecurityUtil;
import com.tradebridge.backend.product.model.ProductDraftResponse;
import com.tradebridge.backend.product.model.ProductResponse;
import com.tradebridge.backend.product.model.UpdateDraftRequest;
import com.tradebridge.backend.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/seller/products/drafts")
public class SellerProductController {

    private final ProductService productService;

    public SellerProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDraftResponse upload(@RequestParam String categoryId, @RequestParam MultipartFile file) {
        return productService.createDraft(SecurityUtil.currentUser(), categoryId, file);
    }

    @GetMapping("/{draftId}")
    public ProductDraftResponse getDraft(@PathVariable String draftId) {
        return productService.getDraft(draftId, SecurityUtil.currentUser());
    }

    @PutMapping("/{draftId}")
    public ProductDraftResponse updateDraft(@PathVariable String draftId, @Valid @RequestBody UpdateDraftRequest request) {
        return productService.updateDraft(draftId, request, SecurityUtil.currentUser());
    }

    @PostMapping("/{draftId}/confirm")
    public ProductResponse confirmDraft(@PathVariable String draftId) {
        return productService.confirmDraft(draftId, SecurityUtil.currentUser());
    }
}
