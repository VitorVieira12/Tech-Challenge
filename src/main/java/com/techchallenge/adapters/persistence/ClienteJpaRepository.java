package com.techchallenge.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para Cliente
 * Interface Spring Data JPA (framework-specific)
 */
@Repository
public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {
    
    Optional<ClienteJpaEntity> findByCpfCnpj(String cpfCnpj);
    
    boolean existsByCpfCnpj(String cpfCnpj);
}


