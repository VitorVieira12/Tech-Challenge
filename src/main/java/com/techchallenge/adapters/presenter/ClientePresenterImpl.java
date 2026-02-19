package com.techchallenge.adapters.presenter;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;
import org.springframework.stereotype.Component;

/**
 * Implementação do Presenter de Cliente
 * Converte entidades de domínio para DTOs de saída
 * (Adapter Pattern)
 */
@Component
public class ClientePresenterImpl implements ClientePresenter {

    @Override
    public ClienteOutputDTO paraOutput(Cliente cliente) {
        return new ClienteOutputDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpfCnpj().getValor(),
            cliente.getContato().getValor(),
            cliente.isAtivo()
        );
    }
}

