package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.exception.DuplicateResourceException;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoInputDTO;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;

/**
 * Caso de uso: Atualizar Veiculo
 */
public class AtualizarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public AtualizarVeiculoUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public VeiculoOutputDTO executar(Long id, VeiculoInputDTO input) {
        // 1. Buscar veículo existente
        Veiculo veiculo = veiculoGateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com ID: " + id));

        // 2. Validar se nova placa já existe (se foi alterada)
        if (!veiculo.getPlaca().getValor().equals(input.getPlaca()) && 
            veiculoGateway.existePorPlaca(input.getPlaca())) {
            throw new DuplicateResourceException("Já existe um veículo cadastrado com esta placa");
        }

        // 3. Atualizar dados
        veiculo.atualizar(
            input.getPlaca(),
            input.getMarca(),
            input.getModelo(),
            input.getAno(),
            input.getClienteId()
        );

        // 4. Salvar através do gateway
        Veiculo veiculoAtualizado = veiculoGateway.salvar(veiculo);

        // 5. Apresentar resultado através do presenter
        return veiculoPresenter.paraOutput(veiculoAtualizado);
    }
}


