package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar Veiculos por Cliente
 */
public class ListarVeiculosPorClienteUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public ListarVeiculosPorClienteUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public List<VeiculoOutputDTO> executar(Long clienteId) {
        List<Veiculo> veiculos = veiculoGateway.listarPorCliente(clienteId);
        
        return veiculos.stream()
            .map(veiculoPresenter::paraOutput)
            .collect(Collectors.toList());
    }
}


