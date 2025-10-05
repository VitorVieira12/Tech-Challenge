package com.techchallenge.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade associativa que representa um serviço dentro de uma Ordem de Serviço.
 * Armazena o preço do serviço no momento da criação da OS (histórico).
 */
@Entity
@Table(name = "ordem_servico_itens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemDeServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /**
     * Preço do serviço no momento da criação da OS.
     * Mantém histórico mesmo que o preço do serviço mude posteriormente.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    /**
     * Quantidade do serviço (normalmente 1, mas pode variar).
     */
    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}

