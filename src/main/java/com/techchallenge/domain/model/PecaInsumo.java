package com.techchallenge.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade que representa Peças e Insumos utilizados nos serviços da oficina.
 * Inclui controle de estoque.
 */
@Entity
@Table(name = "pecas_insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /**
     * Quantidade disponível em estoque.
     * Requisito: controle de estoque de peças e insumos.
     */
    @Column(nullable = false)
    private Integer quantidadeEstoque;
}


