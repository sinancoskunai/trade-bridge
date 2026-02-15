package com.tradebridge.backend.product.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_product_draft")
public class ProductDraftEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "category_id", nullable = false, length = 36)
    private String categoryId;

    @Column(name = "seller_user_id", nullable = false, length = 36)
    private String sellerUserId;

    @Column(name = "seller_company_id", nullable = false, length = 36)
    private String sellerCompanyId;

    @Column(name = "source_file_name", nullable = false, length = 512)
    private String sourceFileName;

    @Column(nullable = false, length = 64)
    private String status;

    @Column(name = "parsed_fields_json", nullable = false, columnDefinition = "TEXT")
    private String parsedFieldsJson;

    @Column(name = "confidence_json", nullable = false, columnDefinition = "TEXT")
    private String confidenceJson;

    @Column(name = "parse_job_id", length = 36)
    private String parseJobId;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(String sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public String getSellerCompanyId() {
        return sellerCompanyId;
    }

    public void setSellerCompanyId(String sellerCompanyId) {
        this.sellerCompanyId = sellerCompanyId;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParsedFieldsJson() {
        return parsedFieldsJson;
    }

    public void setParsedFieldsJson(String parsedFieldsJson) {
        this.parsedFieldsJson = parsedFieldsJson;
    }

    public String getConfidenceJson() {
        return confidenceJson;
    }

    public void setConfidenceJson(String confidenceJson) {
        this.confidenceJson = confidenceJson;
    }

    public String getParseJobId() {
        return parseJobId;
    }

    public void setParseJobId(String parseJobId) {
        this.parseJobId = parseJobId;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
