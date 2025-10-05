package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade Cliente.
 * Fornece operações de CRUD e consultas customizadas.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca um cliente pelo CPF/CNPJ.
     * @param cpfCnpj CPF ou CNPJ do cliente
     * @return Optional contendo o cliente se encontrado
     */
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    /**
     * Verifica se existe um cliente com o CPF/CNPJ informado.
     * @param cpfCnpj CPF ou CNPJ do cliente
     * @return true se existir, false caso contrário
     */
    boolean existsByCpfCnpj(String cpfCnpj);
}



