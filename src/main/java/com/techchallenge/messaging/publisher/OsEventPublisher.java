package com.techchallenge.messaging.publisher;

import com.techchallenge.messaging.config.RabbitMQConfig;
import com.techchallenge.messaging.event.OsCriadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarOsCriada(OsCriadaEvent event) {
        log.info("Publicando evento os.criada para OS {}", event.getOsId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_OS,
                RabbitMQConfig.RK_OS_CRIADA,
                event);
        log.info("Evento os.criada publicado com sucesso para OS {}", event.getOsId());
    }
}
