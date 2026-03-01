CREATE TABLE IF NOT EXISTS chat_session (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project(id),
    owner_actor VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_chat_session_role CHECK (role IN ('product', 'backend', 'frontend', 'test')),
    CONSTRAINT chk_chat_session_status CHECK (status IN ('active', 'archived'))
);

CREATE TABLE IF NOT EXISTS requirement_handoff (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project(id),
    source_session_id UUID NOT NULL REFERENCES chat_session(id),
    version INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    requirement_summary TEXT NOT NULL,
    acceptance_criteria TEXT NOT NULL,
    impact_scope TEXT,
    priority VARCHAR(16) NOT NULL DEFAULT 'P2',
    target_role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'published',
    published_by VARCHAR(64) NOT NULL,
    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accepted_by VARCHAR(64),
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_requirement_handoff_session_version UNIQUE (source_session_id, version),
    CONSTRAINT chk_requirement_handoff_priority CHECK (priority IN ('P0', 'P1', 'P2', 'P3')),
    CONSTRAINT chk_requirement_handoff_role CHECK (target_role IN ('product', 'backend', 'frontend', 'test')),
    CONSTRAINT chk_requirement_handoff_status CHECK (status IN ('published', 'accepted', 'rejected'))
);

CREATE INDEX IF NOT EXISTS idx_chat_session_project_created ON chat_session (project_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_session_owner_created ON chat_session (owner_actor, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_requirement_handoff_project_created ON requirement_handoff (project_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_requirement_handoff_target_status ON requirement_handoff (target_role, status);
