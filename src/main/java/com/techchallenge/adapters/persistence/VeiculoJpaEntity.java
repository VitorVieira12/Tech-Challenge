package com.techchallenge.adapters.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA para Veiculo
 * Separada da entidade de domínio para evitar vazamento de infraestrutura
 */
@Entity
@Table(name = "veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String placa;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
}


