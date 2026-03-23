package com.techchallenge.core.usecase.pecainsumo.gateway;

import com.techchallenge.core.domain.PecaInsumo;
import java.util.List;
import java.util.Optional;

public interface PecaInsumoGateway {
    PecaInsumo salvar(PecaInsumo pecaInsumo);
    Optional<PecaInsumo> buscarPorId(Long id);
    List<PecaInsumo> listarTodos();
    void deletar(Long id);
}


