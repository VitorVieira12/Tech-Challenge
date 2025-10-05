package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para Cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    
    private Long id;
    private String nome;
    private String cpfCnpj;
    private String contato;

    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpfCnpj(),
            cliente.getContato()
        );
    }
}

