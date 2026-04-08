-- ========================================================================
-- Security Context: API Key Management
-- ========================================================================

CREATE TABLE security_api_keys (
    id BIGINT PRIMARY KEY,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    api_secret VARCHAR(128) NOT NULL,
    business_system_name VARCHAR(128) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    permissions TEXT,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_security_api_keys_api_key ON security_api_keys(api_key) WHERE deleted = FALSE;
CREATE INDEX idx_security_api_keys_system ON security_api_keys(business_system_name) WHERE deleted = FALSE;
