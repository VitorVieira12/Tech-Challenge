package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoFalhouEvent {
    private Long osId;
    private String execucaoId;
    private String motivo;
    private LocalDateTime falhouEm;
}
