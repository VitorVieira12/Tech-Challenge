package com.techchallenge.core.domain;

import com.techchallenge.core.domain.valueobject.AnoVeiculo;
import com.techchallenge.core.domain.valueobject.Placa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio Veiculo - livre de frameworks.
 * Representa a essência do negócio sem dependências externas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {
    
    private Long id;
    private Placa placa;
    private String marca;
    private String modelo;
    private AnoVeiculo ano;
    private Long clienteId;  // Referência ao cliente sem acoplamento

    /**
     * Factory method para criar um novo veículo
     */
    public static Veiculo criar(String placa, String marca, String modelo, Integer ano, Long clienteId) {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(new Placa(placa));
        veiculo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setAno(new AnoVeiculo(ano));
        veiculo.setClienteId(clienteId);
        return veiculo;
    }

    /**
     * Atualizar dados do veículo
     */
    public void atualizar(String placa, String marca, String modelo, Integer ano, Long clienteId) {
        this.placa = new Placa(placa);
        this.marca = marca;
        this.modelo = modelo;
        this.ano = new AnoVeiculo(ano);
        this.clienteId = clienteId;
    }

    /**
     * Validar se veículo pode ser removido
     */
    public void validarRemocao() {
        // Aqui viriam regras de negócio
        // Por exemplo: não pode remover se tiver OS em aberto
    }
}


