package com.techchallenge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoramentoDTO {

    private Double tempoMedioExecucaoHoras;

    private Long quantidadeOsFinalizadas;

    private Double tempoMinimoHoras;

    private Double tempoMaximoHoras;
}

