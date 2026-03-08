package com.techchallenge.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para aprovação/recusa de orçamento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprovacaoOrcamentoInputDTO {

    @NotNull(message = "Campo 'aprovado' é obrigatório")
    private Boolean aprovado;

    private String motivoRecusa; // Obrigatório se aprovado = false
}


