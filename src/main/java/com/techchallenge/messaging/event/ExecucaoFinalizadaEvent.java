package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoFinalizadaEvent {
    private Long osId;
    private String execucaoId;
    private LocalDateTime finalizadaEm;
    private String observacoes;
}
