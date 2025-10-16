package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.PecaInsumo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaInsumoResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;

    public static PecaInsumoResponseDTO fromEntity(PecaInsumo pecaInsumo) {
        return new PecaInsumoResponseDTO(
            pecaInsumo.getId(),
            pecaInsumo.getNome(),
            pecaInsumo.getDescricao(),
            pecaInsumo.getPreco(),
            pecaInsumo.getQuantidadeEstoque()
        );
    }
}

