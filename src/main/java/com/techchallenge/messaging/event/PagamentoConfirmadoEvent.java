package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoConfirmadoEvent {
    private Long osId;
    private Long pagamentoId;
    private BigDecimal valorPago;
    private LocalDateTime confirmedAt;
}
