ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
UPDATE users SET password_hash='PLACEHOLDER' WHERE password_hash IS NULL;

ALTER TABLE users
    ALTER COLUMN password_hash SET NOT NULL;