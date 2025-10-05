package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório JPA para a entidade OrdemDeServico.
 * Fornece operações de CRUD e consultas customizadas para gerenciamento de OSs.
 */
@Repository
public interface OrdemDeServicoRepository extends JpaRepository<OrdemDeServico, Long> {

    /**
     * Busca ordens de serviço por status.
     * @param status Status da ordem de serviço
     * @return Lista de ordens de serviço com o status informado
     */
    List<OrdemDeServico> findByStatus(StatusOrdemServico status);

    /**
     * Busca todas as ordens de serviço de um cliente específico.
     * @param clienteId ID do cliente
     * @return Lista de ordens de serviço do cliente
     */
    List<OrdemDeServico> findByClienteId(Long clienteId);

    /**
     * Busca todas as ordens de serviço de um veículo específico.
     * @param veiculoId ID do veículo
     * @return Lista de ordens de serviço do veículo
     */
    List<OrdemDeServico> findByVeiculoId(Long veiculoId);

    /**
     * Busca ordens de serviço criadas em um período específico.
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @return Lista de ordens de serviço no período
     */
    List<OrdemDeServico> findByDataCriacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    /**
     * Busca ordens de serviço por cliente e status.
     * @param clienteId ID do cliente
     * @param status Status da ordem de serviço
     * @return Lista de ordens de serviço
     */
    List<OrdemDeServico> findByClienteIdAndStatus(Long clienteId, StatusOrdemServico status);

    /**
     * Conta o número de ordens de serviço por status.
     * @param status Status da ordem de serviço
     * @return Quantidade de ordens de serviço com o status informado
     */
    @Query("SELECT COUNT(os) FROM OrdemDeServico os WHERE os.status = :status")
    Long countByStatus(StatusOrdemServico status);
}



