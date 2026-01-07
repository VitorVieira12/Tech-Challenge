package com.techchallenge.domain.usecase;

import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use Case: Listar Ordens de Serviço com Ordenação Prioritária
 * 
 * Responsabilidade: Retornar lista de OS em andamento com ordenação específica
 * 
 * Regras de Negócio:
 * 1. EXCLUIR da lista: OS com status FINALIZADA e ENTREGUE
 * 2. Ordenação prioritária por status:
 *    - 1º EM_EXECUCAO
 *    - 2º AGUARDANDO_APROVACAO
 *    - 3º EM_DIAGNOSTICO
 *    - 4º RECEBIDA
 * 3. Dentro de cada status, ordenar por data de criação (mais antigas primeiro)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListarOrdensServicoUseCase {

    private final OrdemDeServicoRepository ordemDeServicoRepository;

    // Mapa de prioridades de status (menor = maior prioridade)
    private static final Map<StatusOrdemServico, Integer> PRIORIDADE_STATUS = Map.of(
            StatusOrdemServico.EM_EXECUCAO, 1,
            StatusOrdemServico.AGUARDANDO_APROVACAO, 2,
            StatusOrdemServico.EM_DIAGNOSTICO, 3,
            StatusOrdemServico.RECEBIDA, 4
    );

    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> executar() {
        log.info("Listando OS em andamento com ordenação prioritária");

        // Buscar OS ativas (excluindo finalizadas e entregues)
        List<OrdemDeServico> ordensAtivas = ordemDeServicoRepository.findOrdensEmAndamento();

        // Aplicar ordenação complexa
        List<OrdemDeServico> ordensOrdenadas = ordensAtivas.stream()
                .sorted(compararPorPrioridadeEData())
                .collect(Collectors.toList());

        log.info("Total de OS em andamento: {} (excluindo finalizadas e entregues)", ordensOrdenadas.size());

        // Converter para DTO
        return ordensOrdenadas.stream()
                .map(OrdemDeServicoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Comparator composto:
     * 1. Primeiro compara por prioridade de status
     * 2. Depois por data de criação (mais antigas primeiro)
     */
    private Comparator<OrdemDeServico> compararPorPrioridadeEData() {
        return Comparator
                .comparing((OrdemDeServico os) -> PRIORIDADE_STATUS.getOrDefault(os.getStatus(), 99))
                .thenComparing(OrdemDeServico::getDataCriacao);
    }
}

