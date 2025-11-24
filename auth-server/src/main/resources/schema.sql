DROP TABLE IF EXISTS consent_permissions CASCADE;
DROP TABLE IF EXISTS consents CASCADE;

 
CREATE TABLE consents (
    consent_id VARCHAR(255) PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    status_update_date_time TIMESTAMP,
    expiration_date_time TIMESTAMP,
    logged_user_document VARCHAR(255),
    logged_user_rel VARCHAR(50),
    business_entity_document VARCHAR(255),   
    business_entity_rel VARCHAR(50),
    transaction_from_date_time TIMESTAMP,
    transaction_to_date_time TIMESTAMP,
    revocation_reason_code VARCHAR(50),
    revocation_reason_detail TEXT,
    revoked_by VARCHAR(255),
    revoked_at TIMESTAMP
);

 
CREATE TABLE consent_permissions (
    id SERIAL PRIMARY KEY,
    consent_id VARCHAR(255) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT fk_consent_permissions 
        FOREIGN KEY (consent_id) 
        REFERENCES consents(consent_id) 
        ON DELETE CASCADE
);
 
CREATE INDEX idx_consents_status ON consents(status);
CREATE INDEX idx_consents_user_id ON consents(user_id);
CREATE INDEX idx_consents_client_id ON consents(client_id);
CREATE INDEX idx_consents_creation_date ON consents(creation_date_time);
CREATE INDEX idx_consents_expiration ON consents(expiration_date_time);
CREATE INDEX idx_consent_permissions_consent_id ON consent_permissions(consent_id);
 
COMMENT ON TABLE consents IS 'Tabela principal de consentimentos OAuth2/OIDC';
COMMENT ON COLUMN consents.business_entity_document IS 'Documento CNPJ da entidade empresarial';
COMMENT ON COLUMN consents.logged_user_document IS 'CPF do usuário logado';