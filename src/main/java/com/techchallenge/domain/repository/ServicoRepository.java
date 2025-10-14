package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Servico.
 * Fornece operações de CRUD e consultas customizadas.
 */
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    /**
     * Busca serviços por descrição (case insensitive).
     * @param descricao Descrição do serviço
     * @return Lista de serviços encontrados
     */
    @Query("SELECT s FROM Servico s WHERE LOWER(s.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Servico> findByDescricaoContainingIgnoreCase(String descricao);
}






