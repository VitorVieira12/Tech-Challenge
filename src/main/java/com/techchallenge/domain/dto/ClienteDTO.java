package com.techchallenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "CPF/CNPJ é obrigatório")
    @Pattern(
        regexp = "^(\\d{11}|\\d{14})$",
        message = "CPF/CNPJ deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ), apenas números"
    )
    private String cpfCnpj;

    @NotBlank(message = "Contato é obrigatório")
    @Size(min = 8, max = 100, message = "Contato deve ter entre 8 e 100 caracteres")
    private String contato;
}

