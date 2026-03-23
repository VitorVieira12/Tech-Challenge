package com.techchallenge.infrastructure.web.dto;

import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta REST para Veiculo
 * Camada de infraestrutura (Web)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoResponseDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private Long clienteId;

    /**
     * Converte do DTO de use case para DTO REST
     */
    public static VeiculoResponseDTO fromOutput(VeiculoOutputDTO output) {
        return new VeiculoResponseDTO(
            output.getId(),
            output.getPlaca(),
            output.getMarca(),
            output.getModelo(),
            output.getAno(),
            output.getClienteId()
        );
    }
}


