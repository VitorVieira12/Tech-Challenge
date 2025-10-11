package com.techchallenge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para consulta pública de OS pelo cliente.
 * Requer CPF/CNPJ + ID da OS para autenticação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPublicaDTO {
    
    @NotNull(message = "O ID da ordem de serviço é obrigatório")
    private Long ordemServicoId;
    
    @NotBlank(message = "O CPF/CNPJ do cliente é obrigatório")
    private String cpfCnpjCliente;
}

