-- ==========================================
-- 📥 SCRIPT PARA ADICIONAR/CORRIGIR DADOS DE TESTE
-- Execute este script no PostgreSQL (container tcc-postgres)
-- ==========================================

-- Primeiro, verifica o CPF correto dos usuários
-- joao.silva tem CPF: 12345678901
-- maria.santos tem CPF: 10987654321

-- ==========================================
-- 🔧 CORRIGIR: user_id deve ser o CPF (que vem no token)
-- ==========================================
UPDATE accounts SET user_id = '12345678901' WHERE user_id = 'user-001';
UPDATE accounts SET user_id = '10987654321' WHERE user_id = 'user-002';

UPDATE credit_cards SET user_id = '12345678901' WHERE user_id = 'user-001';
UPDATE credit_cards SET user_id = '10987654321' WHERE user_id = 'user-002';

-- ==========================================
-- 💰 INSERIR MAIS CONTAS DE TESTE
-- ==========================================
INSERT INTO accounts (id, account_id, user_id, account_number, account_type, balance, available_balance, currency, holder_name, holder_document, branch_code, bank_code, status, created_at, updated_at) VALUES
('acc-004', 'acc-004', '12345678901', '99887-7', 'CONTA_SALARIO', 8500.00, 8500.00, 'BRL', 'Joao Silva', '12345678901', '1234', '001', 'ACTIVE', NOW(), NOW()),
('acc-005', 'acc-005', '12345678901', '11223-3', 'CONTA_PAGAMENTO', 1200.00, 1200.00, 'BRL', 'Joao Silva', '12345678901', '1234', '341', 'ACTIVE', NOW(), NOW()),
('acc-006', 'acc-006', '10987654321', '55667-8', 'CONTA_POUPANCA', 25000.00, 25000.00, 'BRL', 'Maria Santos', '10987654321', '5678', '001', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET 
    user_id = EXCLUDED.user_id,
    balance = EXCLUDED.balance,
    available_balance = EXCLUDED.available_balance,
    updated_at = NOW();

-- ==========================================
-- 💸 INSERIR MAIS TRANSAÇÕES DE TESTE
-- ==========================================
INSERT INTO transactions (id, transaction_id, account_id, transaction_type, type, amount, currency, description, transaction_date, transaction_date_time, balance_after, status, created_at) VALUES
-- Transações João Silva - Conta Corrente
('txn-006', 'txn-006', 'acc-001', 'CREDITO', 'PIX', 2500.00, 'BRL', 'PIX recebido - Freelance', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', 7500.00, 'COMPLETED', NOW()),
('txn-007', 'txn-007', 'acc-001', 'DEBITO', 'BOLETO', -890.00, 'BRL', 'Pagamento Aluguel', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', 6610.00, 'COMPLETED', NOW()),
('txn-008', 'txn-008', 'acc-001', 'DEBITO', 'TED', -1500.00, 'BRL', 'TED para investimento', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', 5110.00, 'COMPLETED', NOW()),
('txn-009', 'txn-009', 'acc-001', 'DEBITO', 'COMPRA_DEBITO', -75.50, 'BRL', 'iFood - Jantar', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 5034.50, 'COMPLETED', NOW()),
('txn-010', 'txn-010', 'acc-001', 'DEBITO', 'COMPRA_DEBITO', -234.50, 'BRL', 'Uber - Corridas', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 4800.00, 'COMPLETED', NOW()),

-- Transações João Silva - Conta Poupança
('txn-011', 'txn-011', 'acc-002', 'CREDITO', 'TRANSFERENCIA', 5000.00, 'BRL', 'Aplicação mensal', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', 20000.50, 'COMPLETED', NOW()),
('txn-012', 'txn-012', 'acc-002', 'CREDITO', 'RENDIMENTO', 150.25, 'BRL', 'Rendimento poupança', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 15150.75, 'COMPLETED', NOW()),

-- Transações Maria Santos
('txn-013', 'txn-013', 'acc-003', 'CREDITO', 'SALARIO', 4500.00, 'BRL', 'Salário mensal', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', 7000.75, 'COMPLETED', NOW()),
('txn-014', 'txn-014', 'acc-003', 'DEBITO', 'PIX', -1200.00, 'BRL', 'PIX - Cartão de crédito', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', 5800.75, 'COMPLETED', NOW()),
('txn-015', 'txn-015', 'acc-003', 'DEBITO', 'COMPRA_DEBITO', -300.00, 'BRL', 'Farmácia', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 5500.75, 'COMPLETED', NOW())
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- 💳 INSERIR MAIS CARTÕES DE CRÉDITO
-- ==========================================
INSERT INTO credit_cards (id, card_id, user_id, card_number, card_holder_name, card_holder_document, expiry_date, card_type, brand, credit_limit, available_limit, status, created_at, updated_at) VALUES
('card-003', 'card-003', '12345678901', '**** **** **** 9012', 'JOAO SILVA', '12345678901', '2029-03-31', 'CREDITO', 'MASTERCARD', 15000.00, 12300.00, 'ACTIVE', NOW(), NOW()),
('card-004', 'card-004', '12345678901', '**** **** **** 3456', 'JOAO SILVA', '12345678901', '2026-09-30', 'CREDITO', 'ELO', 3000.00, 2850.00, 'ACTIVE', NOW(), NOW()),
('card-005', 'card-005', '10987654321', '**** **** **** 7890', 'MARIA SANTOS', '10987654321', '2027-11-30', 'CREDITO', 'VISA', 8000.00, 6500.00, 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET 
    user_id = EXCLUDED.user_id,
    available_limit = EXCLUDED.available_limit,
    updated_at = NOW();

-- ==========================================
-- ✅ VERIFICAR DADOS INSERIDOS
-- ==========================================
-- SELECT * FROM accounts WHERE user_id = '12345678901';
-- SELECT * FROM transactions WHERE account_id IN (SELECT account_id FROM accounts WHERE user_id = '12345678901');
-- SELECT * FROM credit_cards WHERE user_id = '12345678901';