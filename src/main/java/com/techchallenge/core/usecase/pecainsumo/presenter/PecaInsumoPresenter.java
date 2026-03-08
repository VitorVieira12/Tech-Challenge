package com.techchallenge.core.usecase.pecainsumo.presenter;

import com.techchallenge.core.domain.PecaInsumo;
import com.techchallenge.core.usecase.pecainsumo.dto.PecaInsumoOutputDTO;

public interface PecaInsumoPresenter {
    PecaInsumoOutputDTO paraOutput(PecaInsumo pecaInsumo);
}


