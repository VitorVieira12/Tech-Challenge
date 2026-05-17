-- ============================================================
-- SEED DATA - Tech Challenge Fase 4
-- Execute AFTER the services have started (tables must exist)
-- Run: psql -h tech-challenge-db.cgxmk6mmaeyg.us-east-1.rds.amazonaws.com -U postgres -d techdb -f seed-data.sql
-- ============================================================

-- ============================================================
-- DATABASE: techdb (OS Service)
-- ============================================================

-- Clientes
INSERT INTO clientes (nome, cpf_cnpj, contato, tipo_contato, ativo)
VALUES
  ('Vitor Vieira',        '12674781602', 'vitor@email.com',    'EMAIL',    true),
  ('Maria Silva',         '11144477735', 'maria@email.com',    'EMAIL',    true),
  ('João Santos',         '52998224725', '11987654321',        'TELEFONE', true),
  ('Ana Oliveira',        '07134596300', 'ana@email.com',      'EMAIL',    true),
  ('Carlos Souza',        '71428793860', '21912345678',        'TELEFONE', true)
ON CONFLICT (cpf_cnpj) DO NOTHING;

-- Veiculos
INSERT INTO veiculos (placa, marca, modelo, ano, cliente_id)
VALUES
  ('ABC1234', 'Toyota',     'Corolla',  2020, (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602')),
  ('DEF5678', 'Honda',      'Civic',    2019, (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602')),
  ('GHI9012', 'Volkswagen', 'Golf',     2021, (SELECT id FROM clientes WHERE cpf_cnpj = '11144477735')),
  ('JKL3456', 'Ford',       'Ka',       2018, (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725')),
  ('MNO7890', 'Chevrolet',  'Onix',     2022, (SELECT id FROM clientes WHERE cpf_cnpj = '07134596300')),
  ('PQR1B23', 'Fiat',       'Pulse',    2023, (SELECT id FROM clientes WHERE cpf_cnpj = '71428793860'))
ON CONFLICT (placa) DO NOTHING;

-- Servicos
INSERT INTO servicos (descricao, preco)
VALUES
  ('Troca de óleo e filtro',          89.90),
  ('Alinhamento e balanceamento',    120.00),
  ('Revisão dos freios',             250.00),
  ('Troca de correia dentada',       450.00),
  ('Diagnóstico eletrônico',          80.00),
  ('Limpeza de bicos injetores',     200.00),
  ('Troca de pastilhas de freio',    180.00),
  ('Revisão completa 10.000 km',     350.00)
ON CONFLICT DO NOTHING;

-- Pecas e Insumos
INSERT INTO pecas_insumos (nome, descricao, preco, quantidade_estoque)
VALUES
  ('Óleo Motor 5W30 1L',         'Óleo sintético para motor',              25.90,  50),
  ('Filtro de Óleo',             'Filtro de óleo universal',               18.50,  40),
  ('Filtro de Ar',               'Filtro de ar para motor',                32.00,  30),
  ('Pastilha de Freio Dianteira','Par de pastilhas freio dianteiro',       95.00,  20),
  ('Correia Dentada',            'Correia dentada de distribuição',       180.00,  15),
  ('Vela de Ignição',            'Vela de ignição iridium',                45.00,  60),
  ('Fluido de Freio DOT4',       'Fluido de freio 500ml',                  28.00,  25),
  ('Filtro de Combustível',      'Filtro combustível injetores',           42.00,  20)
ON CONFLICT DO NOTHING;

-- ============================================================
-- Confirma os dados inseridos
-- ============================================================
SELECT 'Clientes:' as tabela, COUNT(*) as total FROM clientes
UNION ALL
SELECT 'Veiculos:', COUNT(*) FROM veiculos
UNION ALL
SELECT 'Servicos:', COUNT(*) FROM servicos
UNION ALL
SELECT 'Pecas/Insumos:', COUNT(*) FROM pecas_insumos;
