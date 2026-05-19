package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoGeradoEvent {
    private Long osId;
    private Long orcamentoId;
    private BigDecimal valorTotal;
    private LocalDateTime geradoEm;
}
