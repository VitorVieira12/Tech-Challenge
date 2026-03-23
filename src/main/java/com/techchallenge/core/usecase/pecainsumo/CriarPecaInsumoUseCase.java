package com.techchallenge.core.usecase.pecainsumo;

import com.techchallenge.core.domain.PecaInsumo;
import com.techchallenge.core.domain.valueobject.ValorMonetario;
import com.techchallenge.core.usecase.pecainsumo.dto.PecaInsumoInputDTO;
import com.techchallenge.core.usecase.pecainsumo.dto.PecaInsumoOutputDTO;
import com.techchallenge.core.usecase.pecainsumo.gateway.PecaInsumoGateway;
import com.techchallenge.core.usecase.pecainsumo.presenter.PecaInsumoPresenter;

public class CriarPecaInsumoUseCase {
    private final PecaInsumoGateway gateway;
    private final PecaInsumoPresenter presenter;

    public CriarPecaInsumoUseCase(PecaInsumoGateway gateway, PecaInsumoPresenter presenter) {
        this.gateway = gateway;
        this.presenter = presenter;
    }

    public PecaInsumoOutputDTO executar(PecaInsumoInputDTO input) {
        PecaInsumo peca = PecaInsumo.criar(
            input.getNome(),
            input.getDescricao(),
            new ValorMonetario(input.getPreco()),
            input.getQuantidadeEstoque()
        );
        PecaInsumo pecaSalva = gateway.salvar(peca);
        return presenter.paraOutput(pecaSalva);
    }
}


