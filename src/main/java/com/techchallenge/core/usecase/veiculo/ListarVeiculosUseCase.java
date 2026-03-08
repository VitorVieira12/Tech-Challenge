package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar todos os Veiculos
 */
public class ListarVeiculosUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public ListarVeiculosUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public List<VeiculoOutputDTO> executar() {
        List<Veiculo> veiculos = veiculoGateway.listarTodos();
        
        return veiculos.stream()
            .map(veiculoPresenter::paraOutput)
            .collect(Collectors.toList());
    }
}


