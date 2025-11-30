\c authdb;

-- ==========================================
-- 👥 USERS
-- ==========================================
-- Password for all users: password
-- BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
INSERT INTO users (id, username, password, email, cpf, enabled) VALUES 
('user-001', 'joao.silva', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'joao@email.com', '12345678901', true),
('user-002', 'maria.santos', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'maria@email.com', '10987654321', true),
('user-003', 'empresa.admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@empresa.com', '99988877766', true)
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- 🔐 OAUTH2 CLIENTS
-- ==========================================
INSERT INTO oauth2_registered_client (id, client_id, client_secret, client_name, authorization_grant_types, redirect_uris, scopes) VALUES
('client-001', 'oauth-client', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'TCC Open Finance Client', 'authorization_code,refresh_token', 'http://localhost:8081/login/oauth2/code/tpp-client,http://localhost:8081/callback', 'openid,profile,accounts,consent,transactions,credit-cards-accounts')
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- ✅ CONSENTS
-- ==========================================
INSERT INTO consents (
    consent_id, 
    client_id, 
    user_id,
    status,
    creation_date_time,
    expiration_date_time,
    logged_user_document,
    logged_user_rel,
    business_entity_document,
    business_entity_rel,
    transaction_from_date_time,
    transaction_to_date_time
) VALUES 
(
    'urn:tcc:consent:demo-001',
    'oauth-client',
    'user-001',
    'AUTHORISED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '365 days',
    '12345678901',
    'OWNER',
    '12345678000190',
    'REPRESENTATIVE',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '30 days'
),
(
    'urn:tcc:consent:demo-002',
    'oauth-client',
    'user-002',
    'AWAITING_AUTHORISATION',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '90 days',
    '10987654321',
    'OWNER',
    '98765432000187',
    'OWNER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '90 days'
),
(
    'urn:tcc:consent:demo-003',
    'oauth-client',
    'user-003',
    'AUTHORISED',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP + INTERVAL '180 days',
    '99988877766',
    'REPRESENTATIVE',
    '11223344000155',
    'ADMINISTRATOR',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP + INTERVAL '60 days'
)
ON CONFLICT (consent_id) DO NOTHING;

-- ==========================================
-- 🔒 CONSENT PERMISSIONS
-- ==========================================
INSERT INTO consent_permissions (consent_id, permission) VALUES 
-- Permissions for demo-001
('urn:tcc:consent:demo-001', 'ACCOUNTS_READ'),
('urn:tcc:consent:demo-001', 'ACCOUNTS_BALANCES_READ'),
('urn:tcc:consent:demo-001', 'RESOURCES_READ'),
('urn:tcc:consent:demo-001', 'CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ'),

-- Permissions for demo-002
('urn:tcc:consent:demo-002', 'ACCOUNTS_READ'),
('urn:tcc:consent:demo-002', 'CREDIT_CARDS_ACCOUNTS_READ'),

-- Permissions for demo-003
('urn:tcc:consent:demo-003', 'ACCOUNTS_READ'),
('urn:tcc:consent:demo-003', 'ACCOUNTS_BALANCES_READ'),
('urn:tcc:consent:demo-003', 'ACCOUNTS_TRANSACTIONS_READ'),
('urn:tcc:consent:demo-003', 'CREDIT_CARDS_ACCOUNTS_READ')
ON CONFLICT (consent_id, permission) DO NOTHING;

-- ==========================================
-- 🔑 JWKS KEYS
-- ==========================================
INSERT INTO jwks_keys (kid, algorithm, key_type, public_key, private_key, active) VALUES
('rsa-2024', 'RS256', 'RSA', 'MIIBIjANBgkqhkiG9w0B...', 'MIIEvQIBADANBgkqh...', true),
('dilithium3-2024', 'DILITHIUM3', 'DILITHIUM', 'MIIGzjCCAVKgAwIBAgI...', 'MIIJQwIBADANBgkqhk...', true)
ON CONFLICT (kid) DO NOTHING;

-- ==========================================
-- 💾 RESOURCE SERVER DATA
-- ==========================================
\c resourcedb;

-- Accounts
INSERT INTO accounts (id, account_number, account_type, balance, available_balance, currency, holder_name, holder_document, branch_code, bank_code) VALUES
('acc-001', '12345-6', 'CONTA_CORRENTE', 5000.00, 4800.00, 'BRL', 'João Silva', '12345678901', '1234', '001'),
('acc-002', '78901-2', 'CONTA_POUPANCA', 15000.50, 15000.50, 'BRL', 'João Silva', '12345678901', '1234', '001'),
('acc-003', '45678-9', 'CONTA_CORRENTE', 2500.75, 2500.75, 'BRL', 'Maria Santos', '10987654321', '5678', '001'),
('acc-004', '11223-3', 'CONTA_PAGAMENTO', 10000.00, 9500.00, 'BRL', 'Empresa Admin', '99988877766', '9999', '001')
ON CONFLICT (id) DO NOTHING;

-- Transactions
INSERT INTO transactions (id, account_id, transaction_type, amount, currency, description, transaction_date, balance_after) VALUES
('txn-001', 'acc-001', 'CREDITO', 1000.00, 'BRL', 'Salário', CURRENT_TIMESTAMP - INTERVAL '5 days', 5000.00),
('txn-002', 'acc-001', 'DEBITO', -150.00, 'BRL', 'Supermercado', CURRENT_TIMESTAMP - INTERVAL '3 days', 4850.00),
('txn-003', 'acc-001', 'DEBITO', -50.00, 'BRL', 'Combustível', CURRENT_TIMESTAMP - INTERVAL '1 day', 4800.00),
('txn-004', 'acc-002', 'CREDITO', 500.00, 'BRL', 'Transferência recebida', CURRENT_TIMESTAMP - INTERVAL '2 days', 15000.50),
('txn-005', 'acc-003', 'DEBITO', -200.00, 'BRL', 'Pagamento PIX', CURRENT_TIMESTAMP - INTERVAL '4 days', 2500.75),
('txn-006', 'acc-004', 'CREDITO', 5000.00, 'BRL', 'Receita empresa', CURRENT_TIMESTAMP - INTERVAL '7 days', 10000.00)
ON CONFLICT (id) DO NOTHING;

-- Credit cards
INSERT INTO credit_cards (id, card_number, card_holder_name, card_holder_document, expiry_date, card_type, credit_limit, available_limit, brand) VALUES
('card-001', '**** **** **** 1234', 'João Silva', '12345678901', '2025-12-31', 'CREDITO', 10000.00, 8500.00, 'VISA'),
('card-002', '**** **** **** 5678', 'Maria Santos', '10987654321', '2026-06-30', 'CREDITO', 5000.00, 4200.00, 'MASTERCARD'),
('card-003', '**** **** **** 9012', 'Empresa Admin', '99988877766', '2027-03-31', 'CORPORATIVO', 50000.00, 45000.00, 'AMERICAN EXPRESS')
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- ✅ SUMMARY
-- ==========================================
\c authdb;
\echo ''
\echo '========================================='
\echo '✅ DATA INITIALIZATION COMPLETE!'
\echo '========================================='
\echo 'Users created: 3'
\echo 'OAuth2 clients created: 1'
\echo 'Consents created: 3'
\echo 'Consent permissions created: 10'
\echo 'Accounts created: 4'
\echo 'Transactions created: 6'
\echo 'Credit cards created: 3'
\echo '========================================='
\echo 'Test credentials:'
\echo '  Username: joao.silva / maria.santos / empresa.admin'
\echo '  Password: password'
\echo '  Client ID: oauth-client'
\echo '  Scopes: openid,profile,accounts,consent'
\echo '========================================='