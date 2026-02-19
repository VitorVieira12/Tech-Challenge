package com.techchallenge.core.usecase.cliente;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;

/**
 * Caso de uso: Deletar Cliente
 */
public class DeletarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public DeletarClienteUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public void executar(Long id) {
        // 1. Buscar cliente para validar se existe
        Cliente cliente = clienteGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        // 2. Validar regras de negócio para remoção
        cliente.validarRemocao();

        // 3. Deletar através do gateway
        clienteGateway.deletar(id);
    }
}

