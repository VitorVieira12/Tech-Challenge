package com.techchallenge.core.usecase.cliente.gateway;

import com.techchallenge.core.domain.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Interface Gateway para persistência de Cliente
 * Define o contrato que a camada de infraestrutura deve implementar
 * (Inversão de Dependência - SOLID)
 */
public interface ClienteGateway {
    
    /**
     * Salva um cliente (create ou update)
     */
    Cliente salvar(Cliente cliente);
    
    /**
     * Busca cliente por ID
     */
    Optional<Cliente> buscarPorId(Long id);
    
    /**
     * Busca cliente por CPF/CNPJ
     */
    Optional<Cliente> buscarPorCpfCnpj(String cpfCnpj);
    
    /**
     * Verifica se existe cliente com o CPF/CNPJ
     */
    boolean existePorCpfCnpj(String cpfCnpj);
    
    /**
     * Lista todos os clientes
     */
    List<Cliente> listarTodos();
    
    /**
     * Deleta cliente por ID
     */
    void deletar(Long id);
}


