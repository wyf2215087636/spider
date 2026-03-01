CREATE TABLE IF NOT EXISTS product_doc (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project(id),
    title VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    owner_actor VARCHAR(64) NOT NULL,
    draft_content TEXT NOT NULL DEFAULT '',
    current_version_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_product_doc_status CHECK (status IN ('draft', 'active', 'archived'))
);

CREATE TABLE IF NOT EXISTS product_doc_version (
    id UUID PRIMARY KEY,
    doc_id UUID NOT NULL REFERENCES product_doc(id),
    version_no INTEGER NOT NULL,
    parent_version_id UUID,
    content TEXT NOT NULL,
    change_summary VARCHAR(500) NOT NULL DEFAULT '',
    source_type VARCHAR(32) NOT NULL DEFAULT 'manual',
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_product_doc_version_no CHECK (version_no >= 1),
    CONSTRAINT chk_product_doc_version_source CHECK (source_type IN ('manual', 'ai_confirm', 'rollback')),
    CONSTRAINT uk_product_doc_version UNIQUE (doc_id, version_no)
);

ALTER TABLE product_doc
    ADD CONSTRAINT fk_product_doc_current_version
        FOREIGN KEY (current_version_id) REFERENCES product_doc_version(id);

CREATE INDEX IF NOT EXISTS idx_product_doc_project_created
    ON product_doc (project_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_product_doc_owner_updated
    ON product_doc (owner_actor, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_product_doc_version_doc_created
    ON product_doc_version (doc_id, created_at DESC);
