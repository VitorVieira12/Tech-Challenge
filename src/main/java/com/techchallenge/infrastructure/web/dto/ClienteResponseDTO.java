package com.techchallenge.infrastructure.web.dto;

import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta HTTP para Cliente
 * Separado do DTO de Use Case para flexibilidade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    
    private Long id;
    private String nome;
    private String cpfCnpj;
    private String contato;
    private boolean ativo;

    /**
     * Converte DTO de Use Case para DTO REST
     */
    public static ClienteResponseDTO fromOutput(ClienteOutputDTO output) {
        return new ClienteResponseDTO(
            output.getId(),
            output.getNome(),
            output.getCpfCnpj(),
            output.getContato(),
            output.isAtivo()
        );
    }
}

