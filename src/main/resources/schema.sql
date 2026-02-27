ALTER TABLE users
    ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS review_submitted BOOLEAN;

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS match_radius_km INTEGER;

ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS timezone VARCHAR(100);

UPDATE profiles
SET review_submitted = FALSE
WHERE review_submitted IS NULL;

ALTER TABLE profiles
    ALTER COLUMN review_submitted SET DEFAULT FALSE;

ALTER TABLE profiles
    ALTER COLUMN review_submitted SET NOT NULL;

ALTER TABLE otp_tokens
    DROP CONSTRAINT IF EXISTS otp_tokens_type_check;

ALTER TABLE otp_tokens
    ADD CONSTRAINT otp_tokens_type_check
        CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'LOGIN_2FA'));

ALTER TABLE matches
    DROP CONSTRAINT IF EXISTS matches_status_check;

ALTER TABLE matches
    ADD CONSTRAINT matches_status_check
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'BLOCKED'));

ALTER TABLE date_invites
    ADD COLUMN IF NOT EXISTS screenshot_blocking_required BOOLEAN;

UPDATE date_invites
SET screenshot_blocking_required = FALSE
WHERE screenshot_blocking_required IS NULL;

ALTER TABLE date_invites
    ALTER COLUMN screenshot_blocking_required SET DEFAULT FALSE;

ALTER TABLE date_invites
    ALTER COLUMN screenshot_blocking_required SET NOT NULL;

ALTER TABLE date_invites
    ADD COLUMN IF NOT EXISTS reschedule_count INTEGER;

UPDATE date_invites
SET reschedule_count = 0
WHERE reschedule_count IS NULL;

ALTER TABLE date_invites
    ALTER COLUMN reschedule_count SET DEFAULT 0;

ALTER TABLE date_invites
    ALTER COLUMN reschedule_count SET NOT NULL;
