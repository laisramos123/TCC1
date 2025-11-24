DELETE FROM consent_permissions;
DELETE FROM consents;
 
INSERT INTO consents (
    consent_id,
    client_id,
    user_id,
    status,
    creation_date_time,
    status_update_date_time,
    expiration_date_time,
    logged_user_document,
    logged_user_rel,
    business_entity_document,
    business_entity_rel
) VALUES 
(
    'urn:tcc:consent:test-001',
    'client-test-rsa',
    'user-001',
    'AUTHORISED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '365 days',
    '12345678901',
    'OWNER',
    '12345678000190',
    'REPRESENTATIVE'
),
(
    'urn:tcc:consent:test-002',
    'client-test-dilithium',
    'user-002',
    'AWAITING_AUTHORISATION',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '90 days',
    '98765432109',
    'OWNER',
    '98765432000187',
    'OWNER'
);

 
INSERT INTO consent_permissions (consent_id, permission) VALUES 
('urn:tcc:consent:test-001', 'ACCOUNTS_READ'),
('urn:tcc:consent:test-001', 'ACCOUNTS_BALANCES_READ'),
('urn:tcc:consent:test-001', 'RESOURCES_READ'),
('urn:tcc:consent:test-002', 'ACCOUNTS_READ'),
('urn:tcc:consent:test-002', 'CREDIT_CARDS_ACCOUNTS_READ');

 
DO $$
BEGIN
    RAISE NOTICE 'Schema e dados iniciais carregados com sucesso!';
    RAISE NOTICE 'Consentimentos de teste criados: 2';
    RAISE NOTICE 'Permissões de teste criadas: 5';
END $$;