-- init-scripts/01-create-databases.sql
-- Criar bancos separados para cada serviço

CREATE DATABASE authdb
    WITH OWNER = tcc_user
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

CREATE DATABASE resourcedb  
    WITH OWNER = tcc_user
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

-- Conectar no banco authdb
\c authdb;

-- Usuários de exemplo para AUTH-SERVER
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clientes OAuth2
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(100) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(200) NOT NULL,
    authorization_grant_types TEXT NOT NULL,
    redirect_uris TEXT NOT NULL,
    scopes TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Consentimentos
CREATE TABLE IF NOT EXISTS consents (
    consent_id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    user_document VARCHAR(14) NOT NULL,
    user_document_type VARCHAR(4) NOT NULL,
    business_document VARCHAR(14),
    business_document_type VARCHAR(4),
    status VARCHAR(50) NOT NULL,
    creation_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_update_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration_date_time TIMESTAMP NOT NULL,
    transaction_from_date_time TIMESTAMP,
    transaction_to_date_time TIMESTAMP,
    authorization_code VARCHAR(255),
    access_token_hash VARCHAR(255),
    dilithium_signature TEXT,
    signature_timestamp TIMESTAMP
);

-- Permissões de consentimento
CREATE TABLE IF NOT EXISTS consent_permissions (
    consent_id VARCHAR(255) NOT NULL,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (consent_id, permission),
    FOREIGN KEY (consent_id) REFERENCES consents(consent_id) ON DELETE CASCADE
);

-- Conectar no banco resourcedb
\c resourcedb;

-- Contas bancárias para RESOURCE-SERVER
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(255) PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'BRL',
    holder_name VARCHAR(200) NOT NULL,
    holder_document VARCHAR(14) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transações
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
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Cartões de crédito
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);