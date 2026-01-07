package com.techchallenge.domain.model;

import com.techchallenge.domain.valueobject.ValorMonetario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2))
    private ValorMonetario precoUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", nullable = false, precision = 10, scale = 2))
    private ValorMonetario subtotal;
}

