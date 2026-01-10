package com.techchallenge.domain.usecase;

import com.techchallenge.domain.dto.AprovacaoOrcamentoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case: Aprovar ou Recusar Orçamento
 * 
 * Responsabilidade: Processar a decisão do cliente sobre o orçamento apresentado
 * Regras de Negócio:
 * - Apenas OS no status AGUARDANDO_APROVACAO podem ser aprovadas/recusadas
 * - Se aprovado: muda para EM_EXECUCAO e define dataInicioExecucao
 * - Se recusado: muda para RECEBIDA e adiciona observação
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AprovarOrcamentoUseCase {

    private final OrdemDeServicoRepository ordemDeServicoRepository;
    private final com.techchallenge.domain.service.EmailNotificationService emailNotificationService;

    @Transactional
    public OrdemDeServicoResponseDTO executar(Long osId, AprovacaoOrcamentoInputDTO input) {
        log.info("Processando aprovação de orçamento para OS {}: {}", osId, 
                input.getAprovado() ? "APROVADO" : "RECUSADO");

        // Buscar OS
        OrdemDeServico os = ordemDeServicoRepository.findById(osId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", osId));

        // Validar status atual
        if (os.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException(
                    String.format("Ordem de Serviço %d não está aguardando aprovação. Status atual: %s", 
                            osId, os.getStatus())
            );
        }

        LocalDateTime agora = LocalDateTime.now();

        if (input.getAprovado()) {
            // Orçamento APROVADO - iniciar execução
            os.setStatus(StatusOrdemServico.EM_EXECUCAO);
            os.setDataInicioExecucao(agora);
            
            String observacao = String.format(
                    "[%s] Orçamento APROVADO pelo cliente. Valor: %s. Iniciando execução.", 
                    agora, os.getValorTotalOrcamento().getFormatado()
            );
            adicionarObservacao(os, observacao);
            
            log.info("✅ Orçamento da OS {} APROVADO. Iniciando execução.", osId);
            
        } else {
            // Orçamento RECUSADO - voltar para recebida
            os.setStatus(StatusOrdemServico.RECEBIDA);
            
            String motivoRecusa = input.getMotivoRecusa() != null && !input.getMotivoRecusa().isBlank()
                    ? input.getMotivoRecusa()
                    : "Não informado";
            
            String observacao = String.format(
                    "[%s] Orçamento RECUSADO pelo cliente. Motivo: %s", 
                    agora, motivoRecusa
            );
            adicionarObservacao(os, observacao);
            
            log.warn("❌ Orçamento da OS {} RECUSADO. Motivo: {}", osId, motivoRecusa);
        }

        OrdemDeServico osSalva = ordemDeServicoRepository.save(os);
        
        log.info("Aprovação processada com sucesso para OS {}", osId);
        
        // FASE 2: Enviar notificação por email ao cliente
        emailNotificationService.notificarMudancaStatusOS(osSalva);
        
        return OrdemDeServicoResponseDTO.fromEntity(osSalva);
    }

    private void adicionarObservacao(OrdemDeServico os, String novaObservacao) {
        String observacaoAtual = os.getObservacoes() != null ? os.getObservacoes() : "";
        String observacaoCompleta = observacaoAtual.isBlank() 
                ? novaObservacao 
                : observacaoAtual + "\n" + novaObservacao;
        os.setObservacoes(observacaoCompleta);
    }
}

