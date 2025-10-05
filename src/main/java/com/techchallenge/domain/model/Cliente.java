package com.techchallenge.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um Cliente da oficina.
 * Um cliente pode possuir múltiplos veículos.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /**
     * CPF ou CNPJ do cliente.
     * Pode ser armazenado como String com validação na camada de serviço,
     * ou como Value Object para garantir formato válido.
     */
    @Column(nullable = false, unique = true, length = 14)
    private String cpfCnpj;

    /**
     * Contato do cliente (email/telefone).
     */
    @Column(nullable = false)
    private String contato;

    /**
     * Lista de veículos pertencentes ao cliente.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Veiculo> veiculos = new ArrayList<>();
}


