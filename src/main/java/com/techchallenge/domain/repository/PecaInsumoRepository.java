package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.PecaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade PecaInsumo.
 * Fornece operações de CRUD e consultas customizadas para controle de estoque.
 */
@Repository
public interface PecaInsumoRepository extends JpaRepository<PecaInsumo, Long> {

    /**
     * Busca peças/insumos com estoque baixo (quantidade menor ou igual ao valor informado).
     * @param quantidade Quantidade mínima para considerar estoque baixo
     * @return Lista de peças com estoque baixo
     */
    List<PecaInsumo> findByQuantidadeEstoqueLessThanEqual(Integer quantidade);

    /**
     * Busca peças/insumos por nome (case insensitive).
     * @param nome Nome da peça/insumo
     * @return Lista de peças encontradas
     */
    @Query("SELECT p FROM PecaInsumo p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<PecaInsumo> findByNomeContainingIgnoreCase(String nome);
}






