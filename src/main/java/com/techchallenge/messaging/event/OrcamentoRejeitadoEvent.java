package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoRejeitadoEvent {
    private Long osId;
    private Long orcamentoId;
    private String motivo;
    private LocalDateTime rejeitadoEm;
}
