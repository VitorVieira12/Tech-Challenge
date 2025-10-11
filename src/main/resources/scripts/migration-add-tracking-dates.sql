-- Migration Script: Add Tracking Dates to OrdemDeServico
-- Data: 2025-10-11
-- Descrição: Adiciona campos de data para rastreamento do ciclo de vida das OSs

-- Verificar se as colunas já existem antes de adicionar
-- Caso o banco de dados já tenha OSs existentes

-- Adicionar campo data_inicio_execucao
ALTER TABLE ordens_servico 
ADD COLUMN IF NOT EXISTS data_inicio_execucao TIMESTAMP;

-- Adicionar campo data_finalizacao
ALTER TABLE ordens_servico 
ADD COLUMN IF NOT EXISTS data_finalizacao TIMESTAMP;

-- Adicionar campo data_entrega
ALTER TABLE ordens_servico 
ADD COLUMN IF NOT EXISTS data_entrega TIMESTAMP;

-- Comentários nas colunas para documentação
COMMENT ON COLUMN ordens_servico.data_inicio_execucao IS 'Data e hora em que a OS entrou em execução (status EM_EXECUCAO)';
COMMENT ON COLUMN ordens_servico.data_finalizacao IS 'Data e hora em que a OS foi finalizada (status FINALIZADA)';
COMMENT ON COLUMN ordens_servico.data_entrega IS 'Data e hora em que o veículo foi entregue ao cliente (status ENTREGUE)';

-- Índices para melhorar performance de consultas de monitoramento
CREATE INDEX IF NOT EXISTS idx_ordens_servico_data_inicio_execucao 
ON ordens_servico(data_inicio_execucao);

CREATE INDEX IF NOT EXISTS idx_ordens_servico_data_finalizacao 
ON ordens_servico(data_finalizacao);

-- Índice composto para consultas de monitoramento (OSs finalizadas com datas)
CREATE INDEX IF NOT EXISTS idx_ordens_servico_status_datas 
ON ordens_servico(status, data_inicio_execucao, data_finalizacao);

-- Verificar se a migração foi bem-sucedida
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'ordens_servico' 
  AND column_name IN ('data_inicio_execucao', 'data_finalizacao', 'data_entrega')
ORDER BY ordinal_position;

-- Estatísticas após migração
SELECT 
    COUNT(*) as total_ordens,
    COUNT(data_inicio_execucao) as com_inicio_execucao,
    COUNT(data_finalizacao) as com_finalizacao,
    COUNT(data_entrega) as com_entrega
FROM ordens_servico;

