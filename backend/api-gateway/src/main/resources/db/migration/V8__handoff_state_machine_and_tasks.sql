ALTER TABLE requirement_handoff DROP CONSTRAINT IF EXISTS chk_requirement_handoff_status;
ALTER TABLE requirement_handoff
    ADD CONSTRAINT chk_requirement_handoff_status
        CHECK (status IN (
            'draft',
            'in_review',
            'published',
            'accepted',
            'in_development',
            'in_testing',
            'done',
            'rejected'
        ));

CREATE TABLE IF NOT EXISTS requirement_task (
    id UUID PRIMARY KEY,
    handoff_id UUID NOT NULL REFERENCES requirement_handoff(id),
    project_id UUID NOT NULL REFERENCES project(id),
    role VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    estimate_hours INTEGER NOT NULL DEFAULT 4,
    status VARCHAR(32) NOT NULL DEFAULT 'todo',
    assignee VARCHAR(64),
    source VARCHAR(16) NOT NULL DEFAULT 'ai',
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_requirement_task_role CHECK (role IN ('product', 'backend', 'frontend', 'test')),
    CONSTRAINT chk_requirement_task_status CHECK (status IN ('todo', 'in_progress', 'done', 'blocked')),
    CONSTRAINT chk_requirement_task_source CHECK (source IN ('ai', 'manual')),
    CONSTRAINT chk_requirement_task_estimate CHECK (estimate_hours >= 1 AND estimate_hours <= 200)
);

CREATE INDEX IF NOT EXISTS idx_requirement_task_handoff_sort
    ON requirement_task (handoff_id, sort_order ASC, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_requirement_task_role_status
    ON requirement_task (role, status, created_at DESC);
