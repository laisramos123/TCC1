 
CREATE TABLE IF NOT EXISTS consents (
    consent_id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    status_update_date_time TIMESTAMP,
    expiration_date_time TIMESTAMP NOT NULL,
    logged_user_document VARCHAR(20),
    logged_user_rel VARCHAR(10),
    business_entity_document VARCHAR(20),
    business_entity_rel VARCHAR(10),
    transaction_from_date_time TIMESTAMP,
    transaction_to_date_time TIMESTAMP,
    revocation_reason_code VARCHAR(100),
    revocation_reason_detail VARCHAR(500),
    revoked_by VARCHAR(100),
    revoked_at TIMESTAMP
);

 
CREATE TABLE IF NOT EXISTS consent_permissions (
    consent_id VARCHAR(100) NOT NULL,
    permission VARCHAR(100) NOT NULL,
    FOREIGN KEY (consent_id) REFERENCES consents(consent_id) ON DELETE CASCADE
);

 
CREATE INDEX IF NOT EXISTS idx_consents_client_id ON consents(client_id);
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);
CREATE INDEX IF NOT EXISTS idx_consents_user_document ON consents(logged_user_document);
CREATE INDEX IF NOT EXISTS idx_consents_expiration ON consents(expiration_date_time);
CREATE INDEX IF NOT EXISTS idx_consent_permissions_consent_id ON consent_permissions(consent_id);

 
CREATE TABLE IF NOT EXISTS oauth2_authorization (
    id varchar(100) NOT NULL,
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorization_grant_type varchar(100) NOT NULL,
    authorized_scopes varchar(1000) DEFAULT NULL,
    attributes TEXT DEFAULT NULL,
    state varchar(500) DEFAULT NULL,
    authorization_code_value TEXT DEFAULT NULL,
    authorization_code_issued_at timestamp DEFAULT NULL,
    authorization_code_expires_at timestamp DEFAULT NULL,
    authorization_code_metadata TEXT DEFAULT NULL,
    access_token_value TEXT DEFAULT NULL,
    access_token_issued_at timestamp DEFAULT NULL,
    access_token_expires_at timestamp DEFAULT NULL,
    access_token_metadata TEXT DEFAULT NULL,
    access_token_type varchar(100) DEFAULT NULL,
    access_token_scopes varchar(1000) DEFAULT NULL,
    oidc_id_token_value TEXT DEFAULT NULL,
    oidc_id_token_issued_at timestamp DEFAULT NULL,
    oidc_id_token_expires_at timestamp DEFAULT NULL,
    oidc_id_token_metadata TEXT DEFAULT NULL,
    refresh_token_value TEXT DEFAULT NULL,
    refresh_token_issued_at timestamp DEFAULT NULL,
    refresh_token_expires_at timestamp DEFAULT NULL,
    refresh_token_metadata TEXT DEFAULT NULL,
    user_code_value TEXT DEFAULT NULL,
    user_code_issued_at timestamp DEFAULT NULL,
    user_code_expires_at timestamp DEFAULT NULL,
    user_code_metadata TEXT DEFAULT NULL,
    device_code_value TEXT DEFAULT NULL,
    device_code_issued_at timestamp DEFAULT NULL,
    device_code_expires_at timestamp DEFAULT NULL,
    device_code_metadata TEXT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorities varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

 
CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_principal ON oauth2_authorization(principal_name);
CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_client ON oauth2_authorization(registered_client_id);

 
CREATE TABLE IF NOT EXISTS dilithium_metrics (
    id BIGSERIAL PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    duration_ms BIGINT NOT NULL,
    signature_size INT,
    key_size INT,
    algorithm VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dilithium_metrics_created ON dilithium_metrics(created_at);
CREATE INDEX IF NOT EXISTS idx_dilithium_metrics_operation ON dilithium_metrics(operation_type);

 
CREATE TABLE IF NOT EXISTS dilithium_key_cache (
    key_id VARCHAR(100) PRIMARY KEY,
    public_key TEXT NOT NULL,
    private_key TEXT,
    algorithm VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_primary BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_dilithium_key_cache_expires ON dilithium_key_cache(expires_at);
CREATE INDEX IF NOT EXISTS idx_dilithium_key_cache_primary ON dilithium_key_cache(is_primary);