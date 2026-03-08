package com.techchallenge.core.domain;

import com.techchallenge.core.domain.valueobject.Contato;
import com.techchallenge.core.domain.valueobject.CpfCnpj;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio Cliente - livre de frameworks.
 * Representa a essência do negócio sem dependências externas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    
    private Long id;
    private String nome;
    private CpfCnpj cpfCnpj;
    private Contato contato;
    private boolean ativo;

    /**
     * Factory method para criar um novo cliente
     */
    public static Cliente criar(String nome, String cpfCnpj, String contato) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpfCnpj(new CpfCnpj(cpfCnpj));
        cliente.setContato(new Contato(contato));
        cliente.setAtivo(true);
        return cliente;
    }

    /**
     * Regras de negócio: validar se cliente pode ser removido
     */
    public void validarRemocao() {
        // Aqui viriam regras de negócio
        // Por exemplo: não pode remover se tiver OS em aberto
    }

    /**
     * Regras de negócio: ativar cliente
     */
    public void ativar() {
        this.ativo = true;
    }

    /**
     * Regras de negócio: desativar cliente
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Atualizar dados do cliente
     */
    public void atualizar(String nome, String cpfCnpj, String contato) {
        this.nome = nome;
        this.cpfCnpj = new CpfCnpj(cpfCnpj);
        this.contato = new Contato(contato);
    }
}


