package com.techchallenge.domain.model;

/**
 * Enum que representa os possíveis status de uma Ordem de Serviço.
 */
public enum StatusOrdemServico {
    /**
     * OS foi recebida pela oficina
     */
    RECEBIDA,

    /**
     * Veículo está em diagnóstico
     */
    EM_DIAGNOSTICO,

    /**
     * Aguardando aprovação do orçamento pelo cliente
     */
    AGUARDANDO_APROVACAO,

    /**
     * Serviço está sendo executado
     */
    EM_EXECUCAO,

    /**
     * Serviço foi finalizado
     */
    FINALIZADA,

    /**
     * Veículo foi entregue ao cliente
     */
    ENTREGUE
}


