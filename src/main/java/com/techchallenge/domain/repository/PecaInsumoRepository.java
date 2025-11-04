package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.PecaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PecaInsumoRepository extends JpaRepository<PecaInsumo, Long> {

    List<PecaInsumo> findByQuantidadeEstoqueLessThanEqual(Integer quantidade);

    @Query("SELECT p FROM PecaInsumo p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<PecaInsumo> findByNomeContainingIgnoreCase(String nome);
}