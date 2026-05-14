package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoAprovadoEvent {
    private Long osId;
    private Long orcamentoId;
    private LocalDateTime aprovadoEm;
}
