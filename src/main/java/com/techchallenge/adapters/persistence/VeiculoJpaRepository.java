package com.techchallenge.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para Veiculo
 * Interface do Spring Data JPA
 */
@Repository
public interface VeiculoJpaRepository extends JpaRepository<VeiculoJpaEntity, Long> {
    
    Optional<VeiculoJpaEntity> findByPlaca(String placa);
    
    boolean existsByPlaca(String placa);
    
    List<VeiculoJpaEntity> findByClienteId(Long clienteId);
}


