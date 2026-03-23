package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;

/**
 * Caso de uso: Buscar Veiculo por ID
 */
public class BuscarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public BuscarVeiculoUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public VeiculoOutputDTO executar(Long id) {
        Veiculo veiculo = veiculoGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));
        
        return veiculoPresenter.paraOutput(veiculo);
    }
}


