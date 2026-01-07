package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.Veiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String clienteNome;

    public static VeiculoResponseDTO fromEntity(Veiculo veiculo) {
        return new VeiculoResponseDTO(
            veiculo.getId(),
            veiculo.getPlaca().getValor(),
            veiculo.getMarca(),
            veiculo.getModelo(),
            veiculo.getAno().getValor(),
            veiculo.getCliente().getId(),
            veiculo.getCliente().getNome()
        );
    }
}

