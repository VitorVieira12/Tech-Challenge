package com.techchallenge.messaging.consumer;

import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.messaging.config.RabbitMQConfig;
import com.techchallenge.messaging.event.OrcamentoAprovadoEvent;
import com.techchallenge.messaging.event.OrcamentoGeradoEvent;
import com.techchallenge.messaging.event.OrcamentoRejeitadoEvent;
import com.techchallenge.messaging.event.PagamentoConfirmadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingEventConsumer {

    private final OrdemDeServicoRepository ordemDeServicoRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORCAMENTO_GERADO)
    @Transactional
    public void onOrcamentoGerado(OrcamentoGeradoEvent event) {
        log.info("Evento orcamento.gerado recebido para OS {}", event.getOsId());
        OrdemDeServico os = findOs(event.getOsId());

        if (os.getStatus() == StatusOrdemServico.EM_DIAGNOSTICO) {
            os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
            adicionarObservacao(os, String.format(
                    "[%s] Orçamento #%d gerado pelo Billing Service. Valor: R$ %s. Aguardando aprovação do cliente.",
                    LocalDateTime.now(), event.getOrcamentoId(), event.getValorTotal()));
            ordemDeServicoRepository.save(os);
            log.info("OS {} avançada para AGUARDANDO_APROVACAO", event.getOsId());
        } else {
            log.warn("OS {} está no status {} — ignorando orcamento.gerado", event.getOsId(), os.getStatus());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORCAMENTO_APROVADO)
    @Transactional
    public void onOrcamentoAprovado(OrcamentoAprovadoEvent event) {
        log.info("Evento orcamento.aprovado recebido para OS {}", event.getOsId());
        OrdemDeServico os = findOs(event.getOsId());

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            os.setStatus(StatusOrdemServico.EM_EXECUCAO);
            os.setDataInicioExecucao(event.getAprovadoEm() != null ? event.getAprovadoEm() : LocalDateTime.now());
            adicionarObservacao(os, String.format(
                    "[%s] Orçamento aprovado pelo cliente via Billing Service. Enviado para execução.",
                    LocalDateTime.now()));
            ordemDeServicoRepository.save(os);
            log.info("OS {} avançada para EM_EXECUCAO", event.getOsId());
        } else {
            log.warn("OS {} está no status {} — ignorando orcamento.aprovado", event.getOsId(), os.getStatus());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORCAMENTO_REJEITADO)
    @Transactional
    public void onOrcamentoRejeitado(OrcamentoRejeitadoEvent event) {
        log.info("Evento orcamento.rejeitado recebido para OS {} — motivo: {}", event.getOsId(), event.getMotivo());
        OrdemDeServico os = findOs(event.getOsId());

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            os.setStatus(StatusOrdemServico.CANCELADA);
            adicionarObservacao(os, String.format(
                    "[%s] Orçamento REJEITADO pelo cliente. Motivo: %s. OS cancelada.",
                    LocalDateTime.now(), event.getMotivo()));
            ordemDeServicoRepository.save(os);
            log.info("OS {} CANCELADA por rejeição de orçamento", event.getOsId());
        } else {
            log.warn("OS {} está no status {} — ignorando orcamento.rejeitado", event.getOsId(), os.getStatus());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGAMENTO_CONFIRMADO)
    @Transactional
    public void onPagamentoConfirmado(PagamentoConfirmadoEvent event) {
        log.info("Evento pagamento.confirmado recebido para OS {}", event.getOsId());
        OrdemDeServico os = findOs(event.getOsId());

        adicionarObservacao(os, String.format(
                "[%s] Pagamento #%d confirmado. Valor: R$ %s.",
                LocalDateTime.now(), event.getPagamentoId(), event.getValorPago()));
        ordemDeServicoRepository.save(os);
        log.info("Pagamento confirmado registrado na OS {}", event.getOsId());
    }

    private OrdemDeServico findOs(Long osId) {
        return ordemDeServicoRepository.findById(osId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", osId));
    }

    private void adicionarObservacao(OrdemDeServico os, String texto) {
        String atual = os.getObservacoes() != null ? os.getObservacoes() : "";
        os.setObservacoes(atual.isBlank() ? texto : atual + "\n" + texto);
    }
}
