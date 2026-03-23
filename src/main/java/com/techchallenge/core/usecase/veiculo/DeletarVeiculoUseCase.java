package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;

/**
 * Caso de uso: Deletar Veiculo
 */
public class DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public DeletarVeiculoUseCase(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    public void executar(Long id) {
        // 1. Buscar veículo para validar existência
        Veiculo veiculo = veiculoGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));

        // 2. Validar se pode ser removido
        veiculo.validarRemocao();

        // 3. Deletar através do gateway
        veiculoGateway.deletar(id);
    }
}


