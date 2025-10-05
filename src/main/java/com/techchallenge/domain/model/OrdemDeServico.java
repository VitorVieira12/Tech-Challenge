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
     * Relacionamento Many-to-Many: Uma OS pode ter vários serviços,
     * e um serviço pode estar em várias OSs.
     */
    @ManyToMany
    @JoinTable(
        name = "ordem_servico_servicos",
        joinColumns = @JoinColumn(name = "ordem_servico_id"),
        inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<Servico> servicos = new ArrayList<>();

    /**
     * Relacionamento Many-to-Many: Uma OS pode necessitar de várias peças,
     * e uma peça pode ser usada em várias OSs.
     */
    @ManyToMany
    @JoinTable(
        name = "ordem_servico_pecas",
        joinColumns = @JoinColumn(name = "ordem_servico_id"),
        inverseJoinColumns = @JoinColumn(name = "peca_insumo_id")
    )
    private List<PecaInsumo> pecasInsumos = new ArrayList<>();

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
}


