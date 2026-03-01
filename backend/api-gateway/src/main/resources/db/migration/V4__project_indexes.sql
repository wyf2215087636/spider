CREATE INDEX IF NOT EXISTS idx_project_workspace_id ON project (workspace_id);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON project (created_at DESC);
