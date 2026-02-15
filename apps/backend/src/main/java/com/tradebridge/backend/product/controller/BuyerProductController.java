package com.tradebridge.backend.product.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.product.model.ProductResponse;
import com.tradebridge.backend.product.service.ProductService;

@RestController
@RequestMapping("/buyer/products")
public class BuyerProductController {

    private final ProductService productService;

    public BuyerProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String query) {
        return productService.listProducts(categoryId, query);
    }
}
