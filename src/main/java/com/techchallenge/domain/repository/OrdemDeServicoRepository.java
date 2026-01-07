package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemDeServicoRepository extends JpaRepository<OrdemDeServico, Long> {

    List<OrdemDeServico> findByStatus(StatusOrdemServico status);

    List<OrdemDeServico> findByClienteId(Long clienteId);

    List<OrdemDeServico> findByVeiculoId(Long veiculoId);

    List<OrdemDeServico> findByDataCriacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    List<OrdemDeServico> findByClienteIdAndStatus(Long clienteId, StatusOrdemServico status);

    @Query("SELECT COUNT(os) FROM OrdemDeServico os WHERE os.status = :status")
    Long countByStatus(StatusOrdemServico status);

    /**
     * Busca ordens de serviço em andamento (excluindo finalizadas e entregues)
     * Usada pelo Use Case de listagem ordenada
     */
    @Query("SELECT os FROM OrdemDeServico os " +
           "WHERE os.status NOT IN ('FINALIZADA', 'ENTREGUE') " +
           "ORDER BY os.dataCriacao ASC")
    List<OrdemDeServico> findOrdensEmAndamento();
}