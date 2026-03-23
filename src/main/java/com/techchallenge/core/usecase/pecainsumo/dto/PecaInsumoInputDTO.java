package com.techchallenge.core.usecase.pecainsumo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaInsumoInputDTO {
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;
}


