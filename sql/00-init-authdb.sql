-- ==========================================
-- SCRIPT ÚNICO DE INICIALIZAÇÃO - TCC OPEN FINANCE
-- ==========================================
 
-- ==========================================
--   TABELA: USERS
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_cpf ON users(cpf);

-- ==========================================
--   TABELA: CONSENTS
-- ==========================================
CREATE TABLE IF NOT EXISTS consents (
    consent_id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    status_update_date_time TIMESTAMP,
    expiration_date_time TIMESTAMP NOT NULL,
    logged_user_document VARCHAR(255),
    logged_user_rel VARCHAR(100),
    business_entity_document VARCHAR(255),
    business_entity_rel VARCHAR(100),
    transaction_from_date_time TIMESTAMP,
    transaction_to_date_time TIMESTAMP,
    revocation_reason_code VARCHAR(100),
    revocation_reason_detail TEXT,
    revoked_by VARCHAR(255),
    revoked_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_consents_client_id ON consents(client_id);
CREATE INDEX IF NOT EXISTS idx_consents_user_id ON consents(user_id);
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);

-- ==========================================
--   TABELA: CONSENT_PERMISSIONS

-- ==========================================
CREATE TABLE IF NOT EXISTS consent_permissions (
    consent_id VARCHAR(255) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT fk_consent_permissions_consent 
        FOREIGN KEY (consent_id) 
        REFERENCES consents(consent_id) 
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_consent_permissions_consent_id ON consent_permissions(consent_id);

-- ==========================================
--   TABELA: OAUTH2_REGISTERED_CLIENT
-- ==========================================
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255) UNIQUE NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(255),
    client_secret_expires_at TIMESTAMP,
    client_name VARCHAR(255) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000),
    post_logout_redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

-- ==========================================
--   TABELA: OAUTH2_AUTHORIZATION
-- ==========================================
CREATE TABLE IF NOT EXISTS oauth2_authorization (
    id VARCHAR(255) PRIMARY KEY,
    registered_client_id VARCHAR(255) NOT NULL,
    principal_name VARCHAR(255) NOT NULL,
    authorization_grant_type VARCHAR(255) NOT NULL,
    authorized_scopes VARCHAR(1000),
    attributes TEXT,
    state VARCHAR(500),
    authorization_code_value TEXT,
    authorization_code_issued_at TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata TEXT,
    access_token_value TEXT,
    access_token_issued_at TIMESTAMP,
    access_token_expires_at TIMESTAMP,
    access_token_metadata TEXT,
    access_token_type VARCHAR(255),
    access_token_scopes VARCHAR(1000),
    refresh_token_value TEXT,
    refresh_token_issued_at TIMESTAMP,
    refresh_token_expires_at TIMESTAMP,
    refresh_token_metadata TEXT,
    oidc_id_token_value TEXT,
    oidc_id_token_issued_at TIMESTAMP,
    oidc_id_token_expires_at TIMESTAMP,
    oidc_id_token_metadata TEXT,
    oidc_id_token_claims TEXT,
    user_code_value TEXT,
    user_code_issued_at TIMESTAMP,
    user_code_expires_at TIMESTAMP,
    user_code_metadata TEXT,
    device_code_value TEXT,
    device_code_issued_at TIMESTAMP,
    device_code_expires_at TIMESTAMP,
    device_code_metadata TEXT
);

-- ==========================================
-- 🔐 TABELA: OAUTH2_AUTHORIZATION_CONSENT
-- ==========================================
CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
    registered_client_id VARCHAR(255) NOT NULL,
    principal_name VARCHAR(255) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

-- ==========================================
--   TABELA: ACCOUNTS (Resource Server)
-- ==========================================
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(255) PRIMARY KEY,
    account_id VARCHAR(255),
    user_id VARCHAR(255),
    account_number VARCHAR(255) NOT NULL,
    account_type VARCHAR(100) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL,
    available_balance NUMERIC(15, 2),
    currency VARCHAR(3) DEFAULT 'BRL',
    holder_name VARCHAR(255) NOT NULL,
    holder_document VARCHAR(255) NOT NULL,
    branch_code VARCHAR(50) NOT NULL,
    bank_code VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_holder_document ON accounts(holder_document);

-- ==========================================
--   TABELA: TRANSACTIONS (Resource Server)
-- ==========================================
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(255) PRIMARY KEY,
    transaction_id VARCHAR(255),
    account_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(100) NOT NULL,
    type VARCHAR(100),
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'BRL',
    description TEXT,
    transaction_date TIMESTAMP NOT NULL,
    transaction_date_time TIMESTAMP,
    balance_after NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);

-- ==========================================
--   TABELA: CREDIT_CARDS (Resource Server)
-- ==========================================
CREATE TABLE IF NOT EXISTS credit_cards (
    id VARCHAR(255) PRIMARY KEY,
    card_id VARCHAR(255),
    user_id VARCHAR(255),
    card_number VARCHAR(255) NOT NULL,
    card_holder_name VARCHAR(255) NOT NULL,
    card_holder_document VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    card_type VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    credit_limit NUMERIC(15, 2) NOT NULL,
    available_limit NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_credit_cards_user_id ON credit_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_credit_cards_holder_document ON credit_cards(card_holder_document);

-- ==========================================
--   INSERIR DADOS DE EXEMPLO
-- ==========================================

-- Users (password = 'password' com BCrypt E prefixo {bcrypt})
INSERT INTO users (id, username, password, email, cpf, enabled) VALUES 
('user-001', 'joao.silva', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'joao@email.com', '12345678901', true),
('user-002', 'maria.santos', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'maria@email.com', '10987654321', true)
ON CONFLICT (id) DO NOTHING;

-- OAuth2 Client (com {noop} para DelegatingPasswordEncoder)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_name,
    client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes,
    client_settings, token_settings
) VALUES (
    'client-001',
    'oauth-client',
    CURRENT_TIMESTAMP,
    '{noop}secret',
    'TCC Open Finance Client',
    'client_secret_basic,client_secret_post',
    'authorization_code,refresh_token',
    'http://localhost:8081/callback,http://localhost:8081/authorized',
    'http://localhost:8081',
    'openid,profile,accounts,consent,credit-cards-accounts,customers,resources,payments',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",1800.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",604800.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000]}'
) ON CONFLICT (id) DO NOTHING;

-- Accounts (dados de exemplo)
INSERT INTO accounts (id, account_id, user_id, account_number, account_type, balance, available_balance, currency, holder_name, holder_document, branch_code, bank_code, status, created_at, updated_at) VALUES
('acc-001', 'acc-001', 'user-001', '12345-6', 'CONTA_CORRENTE', 5000.00, 4800.00, 'BRL', 'Joao Silva', '12345678901', '1234', '001', 'ACTIVE', NOW(), NOW()),
('acc-002', 'acc-002', 'user-001', '78901-2', 'CONTA_POUPANCA', 15000.50, 15000.50, 'BRL', 'Joao Silva', '12345678901', '1234', '001', 'ACTIVE', NOW(), NOW()),
('acc-003', 'acc-003', 'user-002', '45678-9', 'CONTA_CORRENTE', 2500.75, 2500.75, 'BRL', 'Maria Santos', '10987654321', '5678', '001', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Transactions (dados de exemplo)
INSERT INTO transactions (id, transaction_id, account_id, transaction_type, type, amount, currency, description, transaction_date, transaction_date_time, balance_after, status, created_at) VALUES
('txn-001', 'txn-001', 'acc-001', 'CREDITO', 'CREDITO', 1000.00, 'BRL', 'Salario', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 5000.00, 'COMPLETED', NOW()),
('txn-002', 'txn-002', 'acc-001', 'DEBITO', 'DEBITO', -150.00, 'BRL', 'Supermercado', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 4850.00, 'COMPLETED', NOW()),
('txn-003', 'txn-003', 'acc-001', 'DEBITO', 'DEBITO', -50.00, 'BRL', 'Combustivel', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 4800.00, 'COMPLETED', NOW()),
('txn-004', 'txn-004', 'acc-002', 'CREDITO', 'CREDITO', 500.00, 'BRL', 'Transferencia recebida', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 15000.50, 'COMPLETED', NOW()),
('txn-005', 'txn-005', 'acc-003', 'DEBITO', 'DEBITO', -200.00, 'BRL', 'Pagamento PIX', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', 2500.75, 'COMPLETED', NOW())
ON CONFLICT (id) DO NOTHING;

-- Credit Cards (dados de exemplo)
INSERT INTO credit_cards (id, card_id, user_id, card_number, card_holder_name, card_holder_document, expiry_date, card_type, brand, credit_limit, available_limit, status, created_at, updated_at) VALUES
('card-001', 'card-001', 'user-001', '**** **** **** 1234', 'Joao Silva', '12345678901', '2027-12-31', 'CREDITO', 'VISA', 10000.00, 8500.00, 'ACTIVE', NOW(), NOW()),
('card-002', 'card-002', 'user-002', '**** **** **** 5678', 'Maria Santos', '10987654321', '2028-06-30', 'CREDITO', 'MASTERCARD', 5000.00, 4200.00, 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
-- ==========================================