CREATE TABLE IF NOT EXISTS product_doc_revision (
    id UUID PRIMARY KEY,
    doc_id UUID NOT NULL REFERENCES product_doc(id),
    source_version_id UUID,
    base_content TEXT NOT NULL,
    candidate_content TEXT NOT NULL,
    instruction TEXT NOT NULL,
    change_summary VARCHAR(500) NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    model_provider VARCHAR(64),
    model_name VARCHAR(128),
    created_by VARCHAR(64) NOT NULL,
    confirmed_by VARCHAR(64),
    confirmed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_product_doc_revision_status CHECK (status IN ('pending', 'confirmed', 'rejected'))
);

CREATE TABLE IF NOT EXISTS product_doc_chat_message (
    id UUID PRIMARY KEY,
    doc_id UUID NOT NULL REFERENCES product_doc(id),
    message_role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_product_doc_chat_message_role CHECK (message_role IN ('user', 'assistant', 'system'))
);

CREATE INDEX IF NOT EXISTS idx_product_doc_revision_doc_created
    ON product_doc_revision (doc_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_product_doc_revision_doc_status_created
    ON product_doc_revision (doc_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_product_doc_chat_message_doc_created
    ON product_doc_chat_message (doc_id, created_at ASC);
