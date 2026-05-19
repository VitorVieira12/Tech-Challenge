package com.techchallenge.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsCriadaEvent {

    private Long osId;
    private String clienteNome;
    private String clienteCpfCnpj;
    private String clienteContato;
    private String veiculoPlaca;
    private String veiculoModelo;
    private BigDecimal valorTotalOrcamento;
    private LocalDateTime dataCriacao;
    private List<ItemServicoEvent> servicos;
    private List<ItemPecaEvent> pecas;
    private String observacoes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemServicoEvent {
        private Long servicoId;
        private String descricao;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPecaEvent {
        private Long pecaInsumoId;
        private String nome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }
}
