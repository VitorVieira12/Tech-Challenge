package com.techchallenge.domain.model;

import com.techchallenge.domain.valueobject.ValorMonetario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco", nullable = false, precision = 10, scale = 2))
    private ValorMonetario preco;

    // Construtor para facilitar a criação com Value Objects
    public Servico(String descricao, ValorMonetario preco) {
        this.descricao = descricao;
        this.preco = preco;
    }
}


