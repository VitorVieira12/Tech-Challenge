package com.techchallenge.domain.dto;

import com.techchallenge.domain.model.OrdemDeServico;
import com.techchallenge.domain.model.StatusOrdemServico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoResponseDTO {
    
    private Long id;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataEntrega;
    private BigDecimal valorTotalOrcamento;
    private StatusOrdemServico status;
    private Long clienteId;
    private String clienteNome;
    private Long veiculoId;
    private String veiculoPlaca;
    private String veiculoModelo;
    private List<ItemServicoResponseDTO> servicos;
    private List<ItemPecaResponseDTO> pecas;
    private String observacoes;

    public static OrdemDeServicoResponseDTO fromEntity(OrdemDeServico os) {
        return new OrdemDeServicoResponseDTO(
            os.getId(),
            os.getDataCriacao(),
            os.getDataInicioExecucao(),
            os.getDataFinalizacao(),
            os.getDataEntrega(),
            os.getValorTotalOrcamento(),
            os.getStatus(),
            os.getCliente().getId(),
            os.getCliente().getNome(),
            os.getVeiculo().getId(),
            os.getVeiculo().getPlaca(),
            os.getVeiculo().getMarca() + " " + os.getVeiculo().getModelo(),
            os.getItensServico().stream()
                .map(ItemServicoResponseDTO::fromEntity)
                .collect(Collectors.toList()),
            os.getItensPeca().stream()
                .map(ItemPecaResponseDTO::fromEntity)
                .collect(Collectors.toList()),
            os.getObservacoes()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemServicoResponseDTO {
        private Long id;
        private Long servicoId;
        private String servicoDescricao;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;

        public static ItemServicoResponseDTO fromEntity(com.techchallenge.domain.model.OrdemServicoItem item) {
            return new ItemServicoResponseDTO(
                item.getId(),
                item.getServico().getId(),
                item.getServico().getDescricao(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPecaResponseDTO {
        private Long id;
        private Long pecaInsumoId;
        private String pecaInsumoNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;

        public static ItemPecaResponseDTO fromEntity(com.techchallenge.domain.model.OrdemServicoPeca item) {
            return new ItemPecaResponseDTO(
                item.getId(),
                item.getPecaInsumo().getId(),
                item.getPecaInsumo().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
            );
        }
    }
}

