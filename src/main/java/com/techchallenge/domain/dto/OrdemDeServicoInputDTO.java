package com.techchallenge.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para criação de uma nova Ordem de Serviço.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoInputDTO {

    @NotBlank(message = "CPF/CNPJ do cliente é obrigatório")
    @Pattern(
        regexp = "^(\\d{11}|\\d{14})$",
        message = "CPF/CNPJ deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ), apenas números"
    )
    private String cpfCnpjCliente;

    @NotNull(message = "Dados do veículo são obrigatórios")
    @Valid
    private VeiculoInputDTO veiculo;

    @Valid
    private List<ItemServicoDTO> servicos = new ArrayList<>();

    @Valid
    private List<ItemPecaDTO> pecas = new ArrayList<>();

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
}

