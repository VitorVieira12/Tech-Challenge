package com.techchallenge.core.usecase.cliente;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteInputDTO;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;
import com.techchallenge.core.usecase.exception.DuplicateResourceException;

/**
 * Caso de uso: Criar Cliente
 * Orquestra a lógica de aplicação sem dependências de framework
 */
public class CriarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public CriarClienteUseCase(ClienteGateway clienteGateway, ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    public ClienteOutputDTO executar(ClienteInputDTO input) {
        // 1. Validar se CPF/CNPJ já existe
        if (clienteGateway.existePorCpfCnpj(input.getCpfCnpj())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com este CPF/CNPJ");
        }

        // 2. Criar entidade de domínio
        Cliente cliente = Cliente.criar(
            input.getNome(),
            input.getCpfCnpj(),
            input.getContato()
        );

        // 3. Salvar através do gateway
        Cliente clienteSalvo = clienteGateway.salvar(cliente);

        // 4. Apresentar resultado através do presenter
        return clientePresenter.paraOutput(clienteSalvo);
    }
}

