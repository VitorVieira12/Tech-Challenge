package com.techchallenge.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma Ordem de Serviço (OS) da oficina.
 * Centraliza informações sobre cliente, veículo, serviços e peças necessárias.
 */
@Entity
@Table(name = "ordens_servico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataInicioExecucao;

    @Column
    private LocalDateTime dataFinalizacao;

    @Column
    private LocalDateTime dataEntrega;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    /**
     * Relacionamento Many-to-One: Uma OS pertence a um cliente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Relacionamento Many-to-One: Uma OS está associada a um veículo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    /**
     * Itens de serviço da OS (com quantidade e preço histórico).
     */
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdemServicoItem> itensServico = new ArrayList<>();

    /**
     * Peças/insumos da OS (com quantidade e preço histórico).
     */
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdemServicoPeca> itensPeca = new ArrayList<>();

    /**
     * Observações adicionais sobre a OS.
     */
    @Column(length = 1000)
    private String observacoes;

    /**
     * Método auxiliar executado antes de persistir a entidade.
     * Define a data de criação automaticamente.
     */
    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusOrdemServico.RECEBIDA;
        }
    }

    /**
     * Método auxiliar para adicionar um item de serviço.
     */
    public void adicionarItemServico(OrdemServicoItem item) {
        itensServico.add(item);
        item.setOrdemServico(this);
    }

    /**
     * Método auxiliar para adicionar um item de peça.
     */
    public void adicionarItemPeca(OrdemServicoPeca item) {
        itensPeca.add(item);
        item.setOrdemServico(this);
    }
}


