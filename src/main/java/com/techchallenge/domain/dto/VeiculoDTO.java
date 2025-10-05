package com.techchallenge.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação e atualização de Veículo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTO {

    @NotBlank(message = "Placa é obrigatória")
    @Pattern(
        regexp = "^[A-Z]{3}\\d{4}$|^[A-Z]{3}\\d[A-Z]\\d{2}$",
        message = "Placa deve seguir o formato brasileiro: ABC1234 (antigo) ou ABC1D23 (Mercosul)"
    )
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 50, message = "Marca deve ter entre 2 e 50 caracteres")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 2, max = 50, message = "Modelo deve ter entre 2 e 50 caracteres")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100")
    private Integer ano;

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;
}

