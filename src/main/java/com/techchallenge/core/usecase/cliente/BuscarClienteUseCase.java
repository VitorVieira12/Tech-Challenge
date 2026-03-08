package com.techchallenge.core.usecase.cliente;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;

/**
 * Caso de uso: Buscar Cliente por ID
 */
public class BuscarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public BuscarClienteUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public ClienteOutputDTO executar(Long id) {
        Cliente cliente = clienteGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        return clientePresenter.paraOutput(cliente);
    }
}


