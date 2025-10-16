package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.Servico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponseDTO {
    
    private Long id;
    private String descricao;
    private BigDecimal preco;

    public static ServicoResponseDTO fromEntity(Servico servico) {
        return new ServicoResponseDTO(
            servico.getId(),
            servico.getDescricao(),
            servico.getPreco()
        );
    }
}

