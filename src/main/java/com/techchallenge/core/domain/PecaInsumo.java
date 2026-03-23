package com.techchallenge.core.domain;

import com.techchallenge.core.domain.valueobject.ValorMonetario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio PecaInsumo - livre de frameworks.
 * Representa a essência do negócio sem dependências externas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaInsumo {
    
    private Long id;
    private String nome;
    private String descricao;
    private ValorMonetario preco;
    private Integer quantidadeEstoque;

    /**
     * Factory method para criar uma nova peça/insumo
     */
    public static PecaInsumo criar(String nome, String descricao, ValorMonetario preco, Integer quantidadeEstoque) {
        PecaInsumo peca = new PecaInsumo();
        peca.setNome(nome);
        peca.setDescricao(descricao);
        peca.setPreco(preco);
        peca.setQuantidadeEstoque(quantidadeEstoque);
        peca.validarEstoque();
        return peca;
    }

    /**
     * Atualizar dados da peça/insumo
     */
    public void atualizar(String nome, String descricao, ValorMonetario preco, Integer quantidadeEstoque) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        validarEstoque();
    }

    /**
     * Ajustar estoque (positivo para entrada, negativo para saída)
     * Regra de negócio importante
     */
    public void ajustarEstoque(Integer quantidade) {
        int novaQuantidade = this.quantidadeEstoque + quantidade;
        if (novaQuantidade < 0) {
            throw new IllegalStateException("Quantidade em estoque não pode ser negativa");
        }
        this.quantidadeEstoque = novaQuantidade;
    }

    /**
     * Verificar se há estoque disponível
     */
    public boolean temEstoqueDisponivel(Integer quantidadeNecessaria) {
        return this.quantidadeEstoque >= quantidadeNecessaria;
    }

    /**
     * Validar estoque
     */
    private void validarEstoque() {
        if (this.quantidadeEstoque < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        }
    }
}


