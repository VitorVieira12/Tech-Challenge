package com.techchallenge.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade associativa que representa uma peça/insumo dentro de uma Ordem de Serviço.
 * Armazena quantidade e preço no momento da criação da OS (histórico).
 */
@Entity
@Table(name = "ordem_servico_pecas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemDeServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peca_insumo_id", nullable = false)
    private PecaInsumo pecaInsumo;

    /**
     * Preço da peça no momento da criação da OS.
     * Mantém histórico mesmo que o preço da peça mude posteriormente.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    /**
     * Quantidade da peça utilizada.
     */
    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}

