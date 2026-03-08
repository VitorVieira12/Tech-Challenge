package com.techchallenge.core.usecase.veiculo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de saída para operações de Veiculo
 * Usado pelos casos de uso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoOutputDTO {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private Long clienteId;
}


