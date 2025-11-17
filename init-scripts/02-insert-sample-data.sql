 
\c authdb;
 
INSERT INTO users (id, username, password, email, cpf, enabled) VALUES 
('user-1', 'joao.silva', '$2a$10$N4QjwbUmpAQU.S4UMLbqeOITaKEhDEkWJEy7P7p4u2qYzJ.wY6zWe', 'joao@email.com', '12345678901', true),
('user-2', 'maria.santos', '$2a$10$N4QjwbUmpAQU.S4UMLbqeOITaKEhDEkWJEy7P7p4u2qYzJ.wY6zWe', 'maria@email.com', '10987654321', true)
ON CONFLICT (id) DO NOTHING;
 
INSERT INTO oauth2_registered_client (id, client_id, client_secret, client_name, authorization_grant_types, redirect_uris, scopes) VALUES
('client-1', 'oauth-client', '$2a$10$N4QjwbUmpAQU.S4UMLbqeOITaKEhDEkWJEy7P7p4u2qYzJ.wY6zWe', 'TCC Open Finance  Client', 'authorization_code,refresh_token', 'http://localhost:8081/login/oauth2/code/tpp-client', 'openid,accounts,transactions,credit-cards-accounts')
ON CONFLICT (id) DO NOTHING;

 
\c resourcedb;

 
INSERT INTO accounts (id, account_number, account_type, balance, currency, holder_name, holder_document, branch_code, bank_code) VALUES
('acc-1', '12345-6', 'CONTA_CORRENTE', 5000.00, 'BRL', 'João Silva', '12345678901', '1234', '001'),
('acc-2', '78901-2', 'CONTA_POUPANCA', 15000.50, 'BRL', 'João Silva', '12345678901', '1234', '001'),
('acc-3', '45678-9', 'CONTA_CORRENTE', 2500.75, 'BRL', 'Maria Santos', '10987654321', '5678', '001')
ON CONFLICT (id) DO NOTHING;

 
INSERT INTO transactions (id, account_id, transaction_type, amount, currency, description, transaction_date, balance_after) VALUES
('txn-1', 'acc-1', 'CREDITO', 1000.00, 'BRL', 'Salário', CURRENT_TIMESTAMP - INTERVAL '5 days', 5000.00),
('txn-2', 'acc-1', 'DEBITO', -150.00, 'BRL', 'Supermercado', CURRENT_TIMESTAMP - INTERVAL '3 days', 4850.00),
('txn-3', 'acc-1', 'DEBITO', -50.00, 'BRL', 'Combustível', CURRENT_TIMESTAMP - INTERVAL '1 day', 4800.00),
('txn-4', 'acc-2', 'CREDITO', 500.00, 'BRL', 'Transferência', CURRENT_TIMESTAMP - INTERVAL '2 days', 15000.50)
ON CONFLICT (id) DO NOTHING;

 
INSERT INTO credit_cards (id, card_number, card_holder_name, card_holder_document, expiry_date, card_type, credit_limit, available_limit, brand) VALUES
('card-1', '**** **** **** 1234', 'João Silva', '12345678901', '2025-12-31', 'CREDITO', 10000.00, 8500.00, 'VISA'),
('card-2', '**** **** **** 5678', 'Maria Santos', '10987654321', '2026-06-30', 'CREDITO', 5000.00, 4200.00, 'MASTERCARD')
ON CONFLICT (id) DO NOTHING;