package com.techchallenge.domain.model;

import com.techchallenge.domain.valueobject.ValorMonetario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total_orcamento", nullable = false, precision = 10, scale = 2))
    private ValorMonetario valorTotalOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdemServicoItem> itensServico = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdemServicoPeca> itensPeca = new ArrayList<>();

    @Column(length = 1000)
    private String observacoes;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusOrdemServico.RECEBIDA;
        }
    }

    public void adicionarItemServico(OrdemServicoItem item) {
        itensServico.add(item);
        item.setOrdemServico(this);
    }

    public void adicionarItemPeca(OrdemServicoPeca item) {
        itensPeca.add(item);
        item.setOrdemServico(this);
    }
}


