package com.techchallenge.core.usecase.pecainsumo;

import com.techchallenge.core.domain.PecaInsumo;
import com.techchallenge.core.usecase.exception.ResourceNotFoundException;
import com.techchallenge.core.usecase.pecainsumo.dto.PecaInsumoOutputDTO;
import com.techchallenge.core.usecase.pecainsumo.gateway.PecaInsumoGateway;
import com.techchallenge.core.usecase.pecainsumo.presenter.PecaInsumoPresenter;

public class AjustarEstoqueUseCase {
    private final PecaInsumoGateway gateway;
    private final PecaInsumoPresenter presenter;

    public AjustarEstoqueUseCase(PecaInsumoGateway gateway, PecaInsumoPresenter presenter) {
        this.gateway = gateway;
        this.presenter = presenter;
    }

    public PecaInsumoOutputDTO executar(Long id, Integer quantidadeAjuste) {
        PecaInsumo peca = gateway.buscarPorId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo não encontrada com ID: " + id));
        
        peca.ajustarEstoque(quantidadeAjuste);
        PecaInsumo pecaAtualizada = gateway.salvar(peca);
        return presenter.paraOutput(pecaAtualizada);
    }
}


