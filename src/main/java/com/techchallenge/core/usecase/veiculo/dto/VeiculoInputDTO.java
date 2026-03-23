package com.techchallenge.core.usecase.veiculo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para operações de Veiculo
 * Usado pelos casos de uso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoInputDTO {
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private Long clienteId;
}


