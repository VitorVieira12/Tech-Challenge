package com.techchallenge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de monitoramento de Ordens de Serviço.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoramentoDTO {
    
    /**
     * Tempo médio de execução em horas.
     */
    private Double tempoMedioExecucaoHoras;
    
    /**
     * Quantidade de ordens de serviço finalizadas consideradas no cálculo.
     */
    private Long quantidadeOsFinalizadas;
    
    /**
     * Tempo mínimo de execução em horas.
     */
    private Double tempoMinimoHoras;
    
    /**
     * Tempo máximo de execução em horas.
     */
    private Double tempoMaximoHoras;
}

