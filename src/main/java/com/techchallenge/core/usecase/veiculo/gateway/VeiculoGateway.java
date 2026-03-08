package com.techchallenge.core.usecase.veiculo.gateway;

import com.techchallenge.core.domain.Veiculo;

import java.util.List;
import java.util.Optional;

/**
 * Interface do Gateway de Veiculo (Port)
 * Define as operações de persistência sem se acoplar à implementação
 */
public interface VeiculoGateway {
    
    Veiculo salvar(Veiculo veiculo);
    
    Optional<Veiculo> buscarPorId(Long id);
    
    Optional<Veiculo> buscarPorPlaca(String placa);
    
    boolean existePorPlaca(String placa);
    
    List<Veiculo> listarTodos();
    
    List<Veiculo> listarPorCliente(Long clienteId);
    
    void deletar(Long id);
}


