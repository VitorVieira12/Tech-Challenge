package com.techchallenge.core.usecase.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de saída para casos de uso de Cliente
 * Livre de anotações de framework
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteOutputDTO {
    private Long id;
    private String nome;
    private String cpfCnpj;
    private String contato;
    private boolean ativo;
}


