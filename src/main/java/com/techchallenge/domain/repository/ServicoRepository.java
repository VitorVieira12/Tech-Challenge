package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query("SELECT s FROM Servico s WHERE LOWER(s.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Servico> findByDescricaoContainingIgnoreCase(String descricao);
}