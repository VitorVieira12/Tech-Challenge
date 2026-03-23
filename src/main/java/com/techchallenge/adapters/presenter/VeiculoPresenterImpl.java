package com.techchallenge.adapters.presenter;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;
import org.springframework.stereotype.Component;

/**
 * Implementação do Presenter de Veiculo
 * Converte entidades de domínio para DTOs de saída
 * (Adapter Pattern)
 */
@Component
public class VeiculoPresenterImpl implements VeiculoPresenter {

    @Override
    public VeiculoOutputDTO paraOutput(Veiculo veiculo) {
        return new VeiculoOutputDTO(
            veiculo.getId(),
            veiculo.getPlaca().getValor(),
            veiculo.getMarca(),
            veiculo.getModelo(),
            veiculo.getAno().getValor(),
            veiculo.getClienteId()
        );
    }
}


