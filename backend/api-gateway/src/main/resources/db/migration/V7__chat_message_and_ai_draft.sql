CREATE TABLE IF NOT EXISTS chat_message (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES chat_session(id),
    message_role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_chat_message_role CHECK (message_role IN ('user', 'assistant', 'system'))
);

CREATE INDEX IF NOT EXISTS idx_chat_message_session_created ON chat_message (session_id, created_at ASC);
