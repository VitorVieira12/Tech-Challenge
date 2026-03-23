package com.techchallenge.core.usecase.veiculo.presenter;

import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;

/**
 * Interface do Presenter de Veiculo
 * Converte entidades de domínio para DTOs de saída
 */
public interface VeiculoPresenter {
    
    VeiculoOutputDTO paraOutput(Veiculo veiculo);
}


