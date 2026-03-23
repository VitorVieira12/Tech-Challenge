package com.techchallenge.core.usecase.cliente;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar todos os Clientes
 */
public class ListarClientesUseCase {

    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public ListarClientesUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public List<ClienteOutputDTO> executar() {
        List<Cliente> clientes = clienteGateway.listarTodos();
        
        return clientes.stream()
            .map(clientePresenter::paraOutput)
            .collect(Collectors.toList());
    }
}


