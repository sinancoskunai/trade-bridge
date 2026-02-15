CREATE TABLE tb_category (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tb_category_attribute (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    attr_key VARCHAR(128) NOT NULL,
    attr_type VARCHAR(64) NOT NULL,
    required BOOLEAN NOT NULL,
    enum_values_json TEXT,
    unit VARCHAR(64),
    filterable BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_category_attr_category FOREIGN KEY (category_id) REFERENCES tb_category(id),
    CONSTRAINT uq_category_attr_key UNIQUE (category_id, attr_key)
);
