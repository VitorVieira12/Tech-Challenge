package com.techchallenge.domain.model;

import com.techchallenge.domain.valueobject.ValorMonetario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco", nullable = false, precision = 10, scale = 2))
    private ValorMonetario preco;

    @Column(nullable = false)
    private Integer quantidadeEstoque;
}


