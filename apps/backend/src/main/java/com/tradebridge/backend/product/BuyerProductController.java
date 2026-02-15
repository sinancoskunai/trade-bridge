package com.tradebridge.backend.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
