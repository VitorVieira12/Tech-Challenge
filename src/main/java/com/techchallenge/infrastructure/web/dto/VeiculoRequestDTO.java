package com.techchallenge.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requisição REST para Veiculo
 * Camada de infraestrutura (Web)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoRequestDTO {

    @NotBlank(message = "Placa é obrigatória")
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    private Integer ano;

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;
}


