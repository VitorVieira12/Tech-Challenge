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
public class OrdemDeServicoPublicDTO {
    
    private Long id;
    private LocalDateTime dataCriacao;
    private StatusOrdemServico status;
    private String veiculoPlaca;
    private String veiculoModelo;
    private BigDecimal valorTotalOrcamento;
    private List<ServicoSimplificadoDTO> servicos;
    private String observacoes;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataEntrega;

    public static OrdemDeServicoPublicDTO fromEntity(OrdemDeServico os) {
        return new OrdemDeServicoPublicDTO(
            os.getId(),
            os.getDataCriacao(),
            os.getStatus(),
            os.getVeiculo().getPlaca().getValor(),
            os.getVeiculo().getMarca() + " " + os.getVeiculo().getModelo(),
            os.getValorTotalOrcamento().getValor(),
            os.getItensServico().stream()
                .map(item -> new ServicoSimplificadoDTO(
                    item.getServico().getDescricao(),
                    item.getQuantidade()
                ))
                .collect(Collectors.toList()),
            os.getObservacoes(),
            os.getDataInicioExecucao(),
            os.getDataFinalizacao(),
            os.getDataEntrega()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicoSimplificadoDTO {
        private String descricao;
        private Integer quantidade;
    }
}

