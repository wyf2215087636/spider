ALTER TABLE workspace
    ADD COLUMN IF NOT EXISTS owner VARCHAR(64) NOT NULL DEFAULT 'system';

ALTER TABLE workspace
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'active';

CREATE INDEX IF NOT EXISTS idx_workspace_created_at ON workspace (created_at DESC);
