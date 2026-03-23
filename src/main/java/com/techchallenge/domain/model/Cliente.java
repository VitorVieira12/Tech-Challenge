package com.techchallenge.domain.model;

import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Embedded
    private CpfCnpj cpfCnpj;

    @Embedded
    private Contato contato;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Veiculo> veiculos = new ArrayList<>();
}


