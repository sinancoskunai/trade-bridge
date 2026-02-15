CREATE TABLE tb_product (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    seller_company_id VARCHAR(36) NOT NULL,
    attributes_json TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tb_product_draft (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    seller_user_id VARCHAR(36) NOT NULL,
    seller_company_id VARCHAR(36) NOT NULL,
    source_file_name VARCHAR(512) NOT NULL,
    status VARCHAR(64) NOT NULL,
    parsed_fields_json TEXT NOT NULL,
    confidence_json TEXT NOT NULL,
    parse_job_id VARCHAR(36),
    last_error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tb_document (
    id VARCHAR(36) PRIMARY KEY,
    draft_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_document_draft FOREIGN KEY (draft_id) REFERENCES tb_product_draft(id)
);

CREATE TABLE tb_parse_job (
    id VARCHAR(36) PRIMARY KEY,
    draft_id VARCHAR(36) NOT NULL,
    status VARCHAR(64) NOT NULL,
    attempts INT NOT NULL,
    last_error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_parse_job_draft FOREIGN KEY (draft_id) REFERENCES tb_product_draft(id)
);

ALTER TABLE tb_product_draft
    ADD CONSTRAINT fk_product_draft_parse_job FOREIGN KEY (parse_job_id) REFERENCES tb_parse_job(id);

CREATE INDEX idx_product_category ON tb_product(category_id);
CREATE INDEX idx_product_draft_seller ON tb_product_draft(seller_user_id);
CREATE INDEX idx_parse_job_status ON tb_parse_job(status);
