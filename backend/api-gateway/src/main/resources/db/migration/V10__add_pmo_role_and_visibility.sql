ALTER TABLE app_user DROP CONSTRAINT IF EXISTS chk_app_user_role;
ALTER TABLE app_user
    ADD CONSTRAINT chk_app_user_role
        CHECK (role IN ('product', 'backend', 'frontend', 'test', 'pmo', 'admin'));

INSERT INTO app_user (id, username, display_name, role, status, password_hash)
VALUES
    (
        '10000000-0000-0000-0000-000000000005',
        'pmo',
        'PMO User',
        'pmo',
        'active',
        'pbkdf2_sha256$600000$qSe1HHljEo0jtT25yi3OHg==$qEOoyddkI/Xh30AX10VxT/wdkE1ix0nuDhrIxbILZos='
    )
ON CONFLICT (username) DO NOTHING;
