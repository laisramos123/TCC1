-- Tornar user_id nullable (consent é criado antes do login)
ALTER TABLE consents ALTER COLUMN user_id DROP NOT NULL;

-- Verificar
\d consents
