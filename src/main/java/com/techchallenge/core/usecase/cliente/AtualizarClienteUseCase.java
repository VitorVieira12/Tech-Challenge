package com.techchallenge.core.usecase.cliente;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteInputDTO;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;
import com.techchallenge.core.usecase.exception.DuplicateResourceException;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;

/**
 * Caso de uso: Atualizar Cliente
 */
public class AtualizarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public AtualizarClienteUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public ClienteOutputDTO executar(Long id, ClienteInputDTO input) {
        // 1. Buscar cliente existente
        Cliente cliente = clienteGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        // 2. Verificar se CPF/CNPJ está sendo alterado e se já existe
        if (!cliente.getCpfCnpj().getValor().equals(input.getCpfCnpj()) &&
            clienteGateway.existePorCpfCnpj(input.getCpfCnpj())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com este CPF/CNPJ");
        }

        // 3. Atualizar dados do cliente
        cliente.atualizar(input.getNome(), input.getCpfCnpj(), input.getContato());

        // 4. Salvar através do gateway
        Cliente clienteAtualizado = clienteGateway.salvar(cliente);

        // 5. Apresentar resultado
        return clientePresenter.paraOutput(clienteAtualizado);
    }
}


