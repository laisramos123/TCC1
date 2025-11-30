-- ==========================================
-- 📁 DATABASE CREATION
-- ==========================================
CREATE DATABASE authdb
    WITH OWNER = tcc_user
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

CREATE DATABASE resourcedb  
    WITH OWNER = tcc_user
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

-- ==========================================
-- 🔐 AUTH SERVER SCHEMA (authdb)
-- ==========================================
\c authdb;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 🔐 SPRING AUTHORIZATION SERVER TABLES
-- ==========================================
-- These tables follow the official Spring Authorization Server schema
-- Reference: https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html

CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes TEXT DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value TEXT DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata TEXT DEFAULT NULL,
    access_token_value TEXT DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata TEXT DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value TEXT DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata TEXT DEFAULT NULL,
    refresh_token_value TEXT DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata TEXT DEFAULT NULL,
    user_code_value TEXT DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata TEXT DEFAULT NULL,
    device_code_value TEXT DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata TEXT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

-- ==========================================
-- ✅ CONSENTS TABLE (Open Finance Brasil)
-- ==========================================
CREATE TABLE IF NOT EXISTS consents (
    consent_id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_update_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration_date_time TIMESTAMP NOT NULL,
    logged_user_document VARCHAR(255), 
    logged_user_rel VARCHAR(50),
    business_entity_document VARCHAR(255),
    business_entity_rel VARCHAR(50),
    transaction_from_date_time TIMESTAMP,
    transaction_to_date_time TIMESTAMP,
    revocation_reason_code VARCHAR(50),
    revocation_reason_detail TEXT,
    revoked_by VARCHAR(255),
    revoked_at TIMESTAMP,
    authorization_code VARCHAR(255),
    access_token_hash VARCHAR(255),
    dilithium_signature TEXT,
    signature_timestamp TIMESTAMP,
    signature_algorithm VARCHAR(50) DEFAULT 'DILITHIUM3',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('AUTHORISED', 'AWAITING_AUTHORISATION', 'REJECTED', 'REVOKED', 'CONSUMED'))
);

CREATE TABLE IF NOT EXISTS consent_permissions (
    id SERIAL PRIMARY KEY,
    consent_id VARCHAR(255) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consent_permissions 
        FOREIGN KEY (consent_id) 
        REFERENCES consents(consent_id)
        ON DELETE CASCADE,
    CONSTRAINT unique_consent_permission 
        UNIQUE (consent_id, permission)
);

-- JWKS keys
CREATE TABLE IF NOT EXISTS jwks_keys (
    kid VARCHAR(255) PRIMARY KEY,
    algorithm VARCHAR(50) NOT NULL,
    key_type VARCHAR(50) NOT NULL,
    public_key TEXT NOT NULL,
    private_key TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- ==========================================
-- 📊 INDEXES (authdb)
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);
CREATE INDEX IF NOT EXISTS idx_consents_user_id ON consents(user_id);
CREATE INDEX IF NOT EXISTS idx_consents_client_id ON consents(client_id);
CREATE INDEX IF NOT EXISTS idx_consents_creation ON consents(creation_date_time);
CREATE INDEX IF NOT EXISTS idx_consents_expiration ON consents(expiration_date_time);
CREATE INDEX IF NOT EXISTS idx_consent_permissions_consent_id ON consent_permissions(consent_id);
CREATE INDEX IF NOT EXISTS idx_users_cpf ON users(cpf);
CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_principal ON oauth2_authorization(principal_name);
CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_client ON oauth2_authorization(registered_client_id);

-- ==========================================
-- 💾 RESOURCE SERVER SCHEMA (resourcedb)
-- ==========================================
\c resourcedb;

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(255) PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'BRL',
    holder_name VARCHAR(200) NOT NULL,
    holder_document VARCHAR(14) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_account_type CHECK (account_type IN ('CONTA_CORRENTE', 'CONTA_POUPANCA', 'CONTA_PAGAMENTO'))
);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(255) PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'BRL',
    description VARCHAR(500),
    transaction_date TIMESTAMP NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Credit cards
CREATE TABLE IF NOT EXISTS credit_cards (
    id VARCHAR(255) PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL,
    card_holder_name VARCHAR(200) NOT NULL,
    card_holder_document VARCHAR(14) NOT NULL,
    expiry_date DATE NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    credit_limit DECIMAL(15,2) NOT NULL,
    available_limit DECIMAL(15,2) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Consent validation cache
CREATE TABLE IF NOT EXISTS consent_validations_cache (
    consent_id VARCHAR(255) PRIMARY KEY,
    is_valid BOOLEAN NOT NULL,
    permissions TEXT,
    validated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- ==========================================
-- 📊 INDEXES (resourcedb)
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_accounts_holder_document ON accounts(holder_document);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_credit_cards_holder_document ON credit_cards(card_holder_document);
CREATE INDEX IF NOT EXISTS idx_consent_cache_expires ON consent_validations_cache(expires_at);

-- ==========================================
-- 💬 COMMENTS
-- ==========================================
\c authdb;
COMMENT ON DATABASE authdb IS 'Banco de dados do Authorization Server - OAuth2/OIDC';
COMMENT ON TABLE consents IS 'Consentimentos OAuth2 com suporte a Dilithium (pós-quântico)';
COMMENT ON TABLE oauth2_registered_client IS 'Clientes OAuth2 registrados - Schema do Spring Authorization Server';
COMMENT ON TABLE oauth2_authorization IS 'Autorizações OAuth2 - Schema do Spring Authorization Server';

\c resourcedb;
COMMENT ON DATABASE resourcedb IS 'Banco de dados do Resource Server - APIs Open Banking';

\echo '✅ Databases and schemas created successfully!'