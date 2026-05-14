# language: pt
Funcionalidade: Ciclo de vida de uma Ordem de Serviço
  Como um usuário do sistema
  Quero criar e acompanhar o ciclo de vida de uma OS
  Para garantir que o fluxo de Saga funcione corretamente

  Contexto:
    Dado que existe um cliente com CPF "12345678901" e nome "João Silva"
    E que existe um serviço com ID 1 e descrição "Troca de óleo" e preço 150.00

  Cenário: Abertura de OS coloca status em EM_DIAGNOSTICO e publica evento
    Quando o cliente abre uma OS com o veículo placa "ABC1234" e o serviço de ID 1
    Então a OS é criada com status "EM_DIAGNOSTICO"
    E o evento "os.criada" é publicado para o Billing Service

  Cenário: OS avança para AGUARDANDO_APROVACAO ao receber evento orcamento.gerado
    Dado que existe uma OS com ID 1 no status "EM_DIAGNOSTICO"
    Quando o Billing Service publica o evento "orcamento.gerado" para a OS 1
    Então a OS 1 tem status "AGUARDANDO_APROVACAO"

  Cenário: OS avança para EM_EXECUCAO ao receber evento orcamento.aprovado
    Dado que existe uma OS com ID 1 no status "AGUARDANDO_APROVACAO"
    Quando o Billing Service publica o evento "orcamento.aprovado" para a OS 1
    Então a OS 1 tem status "EM_EXECUCAO"

  Cenário: OS avança para FINALIZADA ao receber evento execucao.finalizada
    Dado que existe uma OS com ID 1 no status "EM_EXECUCAO"
    Quando o Execution Service publica o evento "execucao.finalizada" para a OS 1
    Então a OS 1 tem status "FINALIZADA"

  Cenário: OS é CANCELADA quando orçamento é rejeitado (rollback Saga)
    Dado que existe uma OS com ID 1 no status "AGUARDANDO_APROVACAO"
    Quando o Billing Service publica o evento "orcamento.rejeitado" para a OS 1 com motivo "Preço elevado"
    Então a OS 1 tem status "CANCELADA"

  Cenário: OS reverte para EM_DIAGNOSTICO quando execução falha (rollback Saga)
    Dado que existe uma OS com ID 1 no status "EM_EXECUCAO"
    Quando o Execution Service publica o evento "execucao.falhou" para a OS 1 com motivo "Peça faltando"
    Então a OS 1 tem status "EM_DIAGNOSTICO"
