ALTER TABLE requirement_task
    ADD COLUMN IF NOT EXISTS title_zh VARCHAR(200),
    ADD COLUMN IF NOT EXISTS title_en VARCHAR(200),
    ADD COLUMN IF NOT EXISTS description_zh TEXT,
    ADD COLUMN IF NOT EXISTS description_en TEXT;

UPDATE requirement_task
SET
    title_zh = COALESCE(title_zh, title),
    title_en = COALESCE(title_en, title),
    description_zh = COALESCE(description_zh, description),
    description_en = COALESCE(description_en, description)
WHERE
    title_zh IS NULL
    OR title_en IS NULL
    OR description_zh IS NULL
    OR description_en IS NULL;

ALTER TABLE requirement_task
    ALTER COLUMN title_zh SET NOT NULL,
    ALTER COLUMN title_en SET NOT NULL,
    ALTER COLUMN description_zh SET NOT NULL,
    ALTER COLUMN description_en SET NOT NULL;
