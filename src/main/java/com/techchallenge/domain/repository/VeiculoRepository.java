package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Veiculo.
 * Fornece operações de CRUD e consultas customizadas.
 */
@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    /**
     * Busca um veículo pela placa.
     * @param placa Placa do veículo
     * @return Optional contendo o veículo se encontrado
     */
    Optional<Veiculo> findByPlaca(String placa);

    /**
     * Busca todos os veículos de um cliente específico.
     * @param clienteId ID do cliente
     * @return Lista de veículos do cliente
     */
    List<Veiculo> findByClienteId(Long clienteId);

    /**
     * Verifica se existe um veículo com a placa informada.
     * @param placa Placa do veículo
     * @return true se existir, false caso contrário
     */
    boolean existsByPlaca(String placa);
}



