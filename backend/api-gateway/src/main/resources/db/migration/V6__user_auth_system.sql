CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    password_hash VARCHAR(300) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_app_user_role CHECK (role IN ('product', 'backend', 'frontend', 'test', 'admin')),
    CONSTRAINT chk_app_user_status CHECK (status IN ('active', 'disabled'))
);

CREATE TABLE IF NOT EXISTS app_user_session (
    token VARCHAR(128) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user (role);
CREATE INDEX IF NOT EXISTS idx_app_user_session_user ON app_user_session (user_id);
CREATE INDEX IF NOT EXISTS idx_app_user_session_expire ON app_user_session (expires_at);

INSERT INTO app_user (id, username, display_name, role, status, password_hash)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'product', 'Product User', 'product', 'active', 'pbkdf2_sha256$600000$Om3dD91gCH5qiptDLNWy5A==$GMV7muU/5ZPPCDEwupj9sdVJFFEunfFSV4Nk8UC7wrg='),
    ('10000000-0000-0000-0000-000000000002', 'backend', 'Backend User', 'backend', 'active', 'pbkdf2_sha256$600000$32UvCYGtxHY8GCBUgfnQgQ==$vQRNIl4DzJOxrMFp7EEffLjtFHmjR9Rm4B556lqjXQI='),
    ('10000000-0000-0000-0000-000000000003', 'frontend', 'Frontend User', 'frontend', 'active', 'pbkdf2_sha256$600000$tF5Kyy86fDOdDb50cnkGuw==$XLRmv9IekSrXmC/9bqCxvIbzTiThYb9b/phuIsaOQaM='),
    ('10000000-0000-0000-0000-000000000004', 'test', 'QA User', 'test', 'active', 'pbkdf2_sha256$600000$rtLNohVeEhRGpX7adYKW8Q==$h9DmBv5cWK2vuVj5iBq57i1ly8EtP4gr8A3NWxvp1JU=')
ON CONFLICT (username) DO NOTHING;
