package com.techchallenge.messaging.consumer;

import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.messaging.config.RabbitMQConfig;
import com.techchallenge.messaging.event.ExecucaoFalhouEvent;
import com.techchallenge.messaging.event.ExecucaoFinalizadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecucaoEventConsumer {

    private final OrdemDeServicoRepository ordemDeServicoRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EXECUCAO_INICIADA)
    @Transactional
    public void onExecucaoIniciada(java.util.Map<String, Object> event) {
        log.info("Evento execucao.iniciada recebido: {}", event.get("osId"));
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EXECUCAO_FINALIZADA)
    @Transactional
    public void onExecucaoFinalizada(ExecucaoFinalizadaEvent event) {
        log.info("Evento execucao.finalizada recebido para OS {}", event.getOsId());
        OrdemDeServico os = findOs(event.getOsId());

        if (os.getStatus() == StatusOrdemServico.EM_EXECUCAO) {
            os.setStatus(StatusOrdemServico.FINALIZADA);
            os.setDataFinalizacao(event.getFinalizadaEm() != null ? event.getFinalizadaEm() : LocalDateTime.now());
            adicionarObservacao(os, String.format(
                    "[%s] Execução finalizada pelo Execution Service. %s",
                    LocalDateTime.now(),
                    event.getObservacoes() != null ? event.getObservacoes() : ""));
            ordemDeServicoRepository.save(os);
            log.info("OS {} avançada para FINALIZADA", event.getOsId());
        } else {
            log.warn("OS {} está no status {} — ignorando execucao.finalizada", event.getOsId(), os.getStatus());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EXECUCAO_FALHOU)
    @Transactional
    public void onExecucaoFalhou(ExecucaoFalhouEvent event) {
        log.info("Evento execucao.falhou recebido para OS {} — motivo: {}", event.getOsId(), event.getMotivo());
        OrdemDeServico os = findOs(event.getOsId());

        if (os.getStatus() == StatusOrdemServico.EM_EXECUCAO) {
            os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
            adicionarObservacao(os, String.format(
                    "[%s] FALHA na execução: %s. OS revertida para diagnóstico (rollback Saga).",
                    LocalDateTime.now(), event.getMotivo()));
            ordemDeServicoRepository.save(os);
            log.warn("OS {} revertida para EM_DIAGNOSTICO por falha na execução (rollback Saga)", event.getOsId());
        } else {
            log.warn("OS {} está no status {} — ignorando execucao.falhou", event.getOsId(), os.getStatus());
        }
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
