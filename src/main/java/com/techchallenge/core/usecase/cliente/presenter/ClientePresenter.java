package com.techchallenge.core.usecase.cliente.presenter;

import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;

/**
 * Interface Presenter para apresentação de Cliente
 * Define o contrato para conversão de entidades de domínio em DTOs de saída
 * (Inversão de Dependência - SOLID)
 */
public interface ClientePresenter {
    
    /**
     * Converte entidade de domínio para DTO de saída
     */
    ClienteOutputDTO paraOutput(Cliente cliente);
}


