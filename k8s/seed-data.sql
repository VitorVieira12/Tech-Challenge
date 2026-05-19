-- ============================================================
-- SEED DATA - Tech Challenge Fase 4
-- Cria as tabelas (se não existirem) e popula com dados de demo
--
-- Como rodar no CloudShell AWS:
--   psql -h tech-challenge-db.cgxmk6mmaeyg.us-east-1.rds.amazonaws.com \
--        -U postgres -d techdb -f seed-data.sql
-- ============================================================

-- ============================================================
-- DDL: Cria as tabelas (Hibernate faz isso no startup,
--      mas assim já temos dados antes de subir os serviços)
-- ============================================================

CREATE TABLE IF NOT EXISTS clientes (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(255)  NOT NULL,
    cpf_cnpj      VARCHAR(14)   NOT NULL UNIQUE,
    contato       VARCHAR(255)  NOT NULL,
    tipo_contato  VARCHAR(20),
    ativo         BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS veiculos (
    id          BIGSERIAL PRIMARY KEY,
    placa       VARCHAR(10)  NOT NULL UNIQUE,
    marca       VARCHAR(255) NOT NULL,
    modelo      VARCHAR(255) NOT NULL,
    ano         INTEGER      NOT NULL,
    cliente_id  BIGINT       NOT NULL REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS servicos (
    id        BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255)    NOT NULL,
    preco     NUMERIC(10, 2)  NOT NULL
);

CREATE TABLE IF NOT EXISTS pecas_insumos (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(255)    NOT NULL,
    descricao           VARCHAR(500),
    preco               NUMERIC(10, 2)  NOT NULL,
    quantidade_estoque  INTEGER         NOT NULL
);

CREATE TABLE IF NOT EXISTS ordens_servico (
    id                      BIGSERIAL PRIMARY KEY,
    data_criacao            TIMESTAMP    NOT NULL,
    data_inicio_execucao    TIMESTAMP,
    data_finalizacao        TIMESTAMP,
    data_entrega            TIMESTAMP,
    valor_total_orcamento   NUMERIC(10, 2) NOT NULL DEFAULT 0,
    status                  VARCHAR(30)  NOT NULL,
    cliente_id              BIGINT       NOT NULL REFERENCES clientes(id),
    veiculo_id              BIGINT       NOT NULL REFERENCES veiculos(id),
    observacoes             VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS ordem_servico_itens (
    id                BIGSERIAL PRIMARY KEY,
    ordem_servico_id  BIGINT          NOT NULL REFERENCES ordens_servico(id),
    servico_id        BIGINT          NOT NULL REFERENCES servicos(id),
    preco_unitario    NUMERIC(10, 2)  NOT NULL,
    quantidade        INTEGER         NOT NULL,
    subtotal          NUMERIC(10, 2)  NOT NULL
);

CREATE TABLE IF NOT EXISTS ordem_servico_pecas (
    id                BIGSERIAL PRIMARY KEY,
    ordem_servico_id  BIGINT          NOT NULL REFERENCES ordens_servico(id),
    peca_insumo_id    BIGINT          NOT NULL REFERENCES pecas_insumos(id),
    preco_unitario    NUMERIC(10, 2)  NOT NULL,
    quantidade        INTEGER         NOT NULL,
    subtotal          NUMERIC(10, 2)  NOT NULL
);

-- ============================================================
-- CLIENTES
-- ============================================================
INSERT INTO clientes (nome, cpf_cnpj, contato, tipo_contato, ativo) VALUES
  ('Vitor Vieira',     '12674781602', 'vitor@email.com',      'EMAIL',    true),
  ('Maria Silva',      '11144477735', 'maria@email.com',      'EMAIL',    true),
  ('João Santos',      '52998224725', '11987654321',          'TELEFONE', true),
  ('Ana Oliveira',     '07134596300', 'ana@email.com',        'EMAIL',    true),
  ('Carlos Souza',     '71428793860', '21912345678',          'TELEFONE', true)
ON CONFLICT (cpf_cnpj) DO NOTHING;

-- ============================================================
-- VEICULOS
-- ============================================================
INSERT INTO veiculos (placa, marca, modelo, ano, cliente_id) VALUES
  ('ABC1234', 'Toyota',     'Corolla',     2020, (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602')),
  ('DEF5678', 'Honda',      'Civic',       2019, (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602')),
  ('GHI9012', 'Volkswagen', 'Golf',        2021, (SELECT id FROM clientes WHERE cpf_cnpj = '11144477735')),
  ('JKL3456', 'Ford',       'Ka',          2018, (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725')),
  ('MNO7890', 'Chevrolet',  'Onix',        2022, (SELECT id FROM clientes WHERE cpf_cnpj = '07134596300')),
  ('PQR1B23', 'Fiat',       'Pulse',       2023, (SELECT id FROM clientes WHERE cpf_cnpj = '71428793860')),
  ('STU4C56', 'Hyundai',    'HB20',        2022, (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602')),
  ('VWX7D89', 'Renault',    'Kwid',        2021, (SELECT id FROM clientes WHERE cpf_cnpj = '11144477735'))
ON CONFLICT (placa) DO NOTHING;

-- ============================================================
-- SERVICOS
-- ============================================================
INSERT INTO servicos (descricao, preco) VALUES
  ('Troca de óleo e filtro',               89.90),
  ('Alinhamento e balanceamento',          120.00),
  ('Revisão dos freios',                   250.00),
  ('Troca de correia dentada',             450.00),
  ('Diagnóstico eletrônico',               80.00),
  ('Limpeza de bicos injetores',           200.00),
  ('Troca de pastilhas de freio',          180.00),
  ('Revisão completa 10.000 km',           350.00),
  ('Troca de amortecedores (par)',         480.00),
  ('Higienização do ar-condicionado',       95.00),
  ('Troca de pneus (unidade)',             180.00),
  ('Polimento e cristalização',            320.00);

-- ============================================================
-- PEÇAS E INSUMOS
-- ============================================================
INSERT INTO pecas_insumos (nome, descricao, preco, quantidade_estoque) VALUES
  ('Óleo Motor 5W30 1L',          'Óleo sintético para motor',                   25.90,  50),
  ('Óleo Motor 10W40 1L',         'Óleo semissintético para motor',              19.90,  60),
  ('Filtro de Óleo',              'Filtro de óleo universal',                    18.50,  40),
  ('Filtro de Ar',                'Filtro de ar para motor',                     32.00,  30),
  ('Filtro de Combustível',       'Filtro combustível para injetores',           42.00,  20),
  ('Filtro de Ar-condicionado',   'Filtro cabine ar-condicionado',               35.00,  25),
  ('Pastilha de Freio Dianteira', 'Par de pastilhas freio dianteiro',            95.00,  20),
  ('Pastilha de Freio Traseira',  'Par de pastilhas freio traseiro',             85.00,  20),
  ('Disco de Freio Dianteiro',    'Par de discos freio dianteiro',              280.00,  10),
  ('Correia Dentada',             'Correia dentada de distribuição',            180.00,  15),
  ('Kit Tensor Correia',          'Kit tensor e esticador correia dentada',      95.00,  12),
  ('Vela de Ignição Iridium',     'Vela de ignição iridium (unidade)',           45.00,  60),
  ('Fluido de Freio DOT4',        'Fluido de freio 500ml',                       28.00,  25),
  ('Fluido Aditivo Radiador',     'Aditivo para sistema de arrefecimento 1L',    32.00,  30),
  ('Amortecedor Dianteiro',       'Amortecedor dianteiro (unidade)',            320.00,   8),
  ('Amortecedor Traseiro',        'Amortecedor traseiro (unidade)',             280.00,   8),
  ('Pneu 185/65 R15',             'Pneu aro 15 para passeio',                   380.00,  12),
  ('Pneu 195/55 R15',             'Pneu aro 15 para passeio',                   420.00,  10),
  ('Rolamento Roda Dianteiro',    'Rolamento roda dianteira (unidade)',          180.00,  10),
  ('Bucha Bandeja',               'Bucha de bandeja suspensão (par)',             65.00,  15);

-- ============================================================
-- ORDENS DE SERVIÇO (exemplos em vários status para o vídeo)
-- ============================================================

-- OS 1: RECEBIDA (recém criada)
INSERT INTO ordens_servico (data_criacao, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '2 hours',
  0.00,
  'RECEBIDA',
  (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602'),
  (SELECT id FROM veiculos WHERE placa = 'ABC1234'),
  'Cliente relatou barulho no motor ao ligar pela manhã'
);

-- OS 2: EM_DIAGNOSTICO
INSERT INTO ordens_servico (data_criacao, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '1 day',
  0.00,
  'EM_DIAGNOSTICO',
  (SELECT id FROM clientes WHERE cpf_cnpj = '11144477735'),
  (SELECT id FROM veiculos WHERE placa = 'GHI9012'),
  'Revisão geral dos 30.000 km'
);

-- OS 3: AGUARDANDO_APROVACAO (com orçamento)
INSERT INTO ordens_servico (data_criacao, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '2 days',
  539.80,
  'AGUARDANDO_APROVACAO',
  (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725'),
  (SELECT id FROM veiculos WHERE placa = 'JKL3456'),
  'Troca de pastilhas e revisão freios necessária'
);

-- Itens da OS 3
INSERT INTO ordem_servico_itens (ordem_servico_id, servico_id, preco_unitario, quantidade, subtotal)
VALUES (
  (SELECT id FROM ordens_servico WHERE cliente_id = (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725') AND status = 'AGUARDANDO_APROVACAO'),
  (SELECT id FROM servicos WHERE descricao = 'Revisão dos freios'),
  250.00, 1, 250.00
);

INSERT INTO ordem_servico_pecas (ordem_servico_id, peca_insumo_id, preco_unitario, quantidade, subtotal)
VALUES (
  (SELECT id FROM ordens_servico WHERE cliente_id = (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725') AND status = 'AGUARDANDO_APROVACAO'),
  (SELECT id FROM pecas_insumos WHERE nome = 'Pastilha de Freio Dianteira'),
  95.00, 1, 95.00
),
(
  (SELECT id FROM ordens_servico WHERE cliente_id = (SELECT id FROM clientes WHERE cpf_cnpj = '52998224725') AND status = 'AGUARDANDO_APROVACAO'),
  (SELECT id FROM pecas_insumos WHERE nome = 'Fluido de Freio DOT4'),
  28.00, 2, 56.00
);

-- OS 4: EM_EXECUCAO
INSERT INTO ordens_servico (data_criacao, data_inicio_execucao, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '1 day',
  629.80,
  'EM_EXECUCAO',
  (SELECT id FROM clientes WHERE cpf_cnpj = '07134596300'),
  (SELECT id FROM veiculos WHERE placa = 'MNO7890'),
  'Troca de correia dentada e kit tensor'
);

-- OS 5: FINALIZADA
INSERT INTO ordens_servico (data_criacao, data_inicio_execucao, data_finalizacao, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '7 days',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '4 days',
  209.80,
  'FINALIZADA',
  (SELECT id FROM clientes WHERE cpf_cnpj = '71428793860'),
  (SELECT id FROM veiculos WHERE placa = 'PQR1B23'),
  'Revisão de rotina concluída com sucesso'
);

-- OS 6: ENTREGUE (completo)
INSERT INTO ordens_servico (data_criacao, data_inicio_execucao, data_finalizacao, data_entrega, valor_total_orcamento, status, cliente_id, veiculo_id, observacoes)
VALUES (
  NOW() - INTERVAL '10 days',
  NOW() - INTERVAL '9 days',
  NOW() - INTERVAL '8 days',
  NOW() - INTERVAL '7 days',
  89.90,
  'ENTREGUE',
  (SELECT id FROM clientes WHERE cpf_cnpj = '12674781602'),
  (SELECT id FROM veiculos WHERE placa = 'DEF5678'),
  'Troca de óleo realizada. Próxima revisão em 5.000 km.'
);

-- ============================================================
-- Resumo do que foi inserido
-- ============================================================
SELECT '=== DADOS INSERIDOS ===' as info;
SELECT 'Clientes'        as tabela, COUNT(*) as total FROM clientes
UNION ALL
SELECT 'Veiculos',                  COUNT(*) FROM veiculos
UNION ALL
SELECT 'Servicos',                  COUNT(*) FROM servicos
UNION ALL
SELECT 'Pecas/Insumos',             COUNT(*) FROM pecas_insumos
UNION ALL
SELECT 'Ordens de Servico',         COUNT(*) FROM ordens_servico;

SELECT '=== STATUS DAS OS ===' as info;
SELECT status, COUNT(*) as quantidade FROM ordens_servico GROUP BY status ORDER BY status;
