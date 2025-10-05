package com.techchallenge.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para criação e atualização de Serviço.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicoDTO {

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 5, max = 200, message = "Descrição deve ter entre 5 e 200 caracteres")
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal preco;
}

