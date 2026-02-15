package com.tradebridge.backend.product.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tradebridge.backend.auth.model.AuthenticatedUser;
import com.tradebridge.backend.product.model.ProductDraftResponse;
import com.tradebridge.backend.product.model.ProductResponse;
import com.tradebridge.backend.product.model.UpdateDraftRequest;

public interface ProductService {

    ProductDraftResponse createDraft(AuthenticatedUser user, String categoryId, MultipartFile file);

    ProductDraftResponse getDraft(String draftId, AuthenticatedUser user);

    ProductDraftResponse updateDraft(String draftId, UpdateDraftRequest request, AuthenticatedUser user);

    ProductResponse confirmDraft(String draftId, AuthenticatedUser user);

    List<ProductResponse> listProducts(String categoryId, String query);
}
