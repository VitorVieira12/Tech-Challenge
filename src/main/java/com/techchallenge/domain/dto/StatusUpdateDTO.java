package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDTO {
    
    @NotNull(message = "O status é obrigatório")
    private StatusOrdemServico novoStatus;
    
    private String observacao;
}

