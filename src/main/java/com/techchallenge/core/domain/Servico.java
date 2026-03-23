package com.techchallenge.core.domain;

import com.techchallenge.core.domain.valueobject.ValorMonetario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servico {
    private Long id;
    private String descricao;
    private ValorMonetario preco;

    public static Servico criar(String descricao, ValorMonetario preco) {
        Servico servico = new Servico();
        servico.setDescricao(descricao);
        servico.setPreco(preco);
        return servico;
    }

    public void atualizar(String descricao, ValorMonetario preco) {
        this.descricao = descricao;
        this.preco = preco;
    }
}


