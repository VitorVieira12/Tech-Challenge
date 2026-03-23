package com.techchallenge.core.usecase.veiculo;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.exception.DuplicateResourceException;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoInputDTO;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;

/**
 * Caso de uso: Criar Veiculo
 * Orquestra a lógica de aplicação sem dependências de framework
 */
public class CriarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoPresenter veiculoPresenter;

    public CriarVeiculoUseCase(VeiculoGateway veiculoGateway, VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    public VeiculoOutputDTO executar(VeiculoInputDTO input) {
        // 1. Validar se placa já existe
        if (veiculoGateway.existePorPlaca(input.getPlaca())) {
            throw new DuplicateResourceException("Já existe um veículo cadastrado com esta placa");
        }

        // 2. Criar entidade de domínio
        Veiculo veiculo = Veiculo.criar(
            input.getPlaca(),
            input.getMarca(),
            input.getModelo(),
            input.getAno(),
            input.getClienteId()
        );

        // 3. Salvar através do gateway
        Veiculo veiculoSalvo = veiculoGateway.salvar(veiculo);

        // 4. Apresentar resultado através do presenter
        return veiculoPresenter.paraOutput(veiculoSalvo);
    }
}


