package com.techchallenge.controller;

import com.techchallenge.domain.dto.AprovacaoOrcamentoInputDTO;
import com.techchallenge.domain.dto.MonitoramentoDTO;
import com.techchallenge.domain.dto.OrdemDeServicoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoPublicDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.dto.StatusUpdateDTO;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.service.OrdemDeServicoService;
import com.techchallenge.domain.usecase.AprovarOrcamentoUseCase;
import com.techchallenge.domain.usecase.ListarOrdensServicoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Gerenciamento completo de Ordens de Serviço")
public class OrdemDeServicoController {

    private final OrdemDeServicoService ordemDeServicoService;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;

    @PostMapping
    public ResponseEntity<OrdemDeServicoResponseDTO> criar(@Valid @RequestBody OrdemDeServicoInputDTO dto) {
        OrdemDeServicoResponseDTO response = ordemDeServicoService.criarOS(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemDeServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        OrdemDeServicoResponseDTO response = ordemDeServicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrdemDeServicoResponseDTO>> listar(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(required = false) Long clienteId) {
        
        List<OrdemDeServicoResponseDTO> response;
        
        if (status != null) {
            response = ordemDeServicoService.listarPorStatus(status);
        } else if (clienteId != null) {
            response = ordemDeServicoService.listarPorCliente(clienteId);
        } else {
            response = ordemDeServicoService.listarTodas();
        }
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemDeServicoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusUpdateDTO) {
        
        OrdemDeServicoResponseDTO response = ordemDeServicoService.atualizarStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{id}")
    @Operation(
            summary = "Consultar status de OS (público)",
            description = "Endpoint público para clientes consultarem suas OSs. Requer CPF/CNPJ para autenticação.",
            security = {} // Remove required authentication for this endpoint
    )
    public ResponseEntity<OrdemDeServicoPublicDTO> consultarStatusPublico(
            @PathVariable Long id,
            @RequestParam String cpfCnpj) {
        
        OrdemDeServicoPublicDTO response = ordemDeServicoService.consultarStatusPublico(id, cpfCnpj);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monitoramento/tempo-medio")
    public ResponseEntity<MonitoramentoDTO> consultarTempoMedio() {
        MonitoramentoDTO response = ordemDeServicoService.calcularTempoMedioExecucao();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/aprovar-orcamento")
    @Operation(
            summary = "Aprovar ou Recusar Orçamento",
            description = "Endpoint para o cliente aprovar ou recusar o orçamento apresentado. " +
                    "Se aprovado, a OS passa para EM_EXECUCAO. Se recusado, volta para RECEBIDA."
    )
    public ResponseEntity<OrdemDeServicoResponseDTO> aprovarOrcamento(
            @PathVariable Long id,
            @Valid @RequestBody AprovacaoOrcamentoInputDTO aprovacaoDTO) {
        
        OrdemDeServicoResponseDTO response = aprovarOrcamentoUseCase.executar(id, aprovacaoDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/em-andamento")
    @Operation(
            summary = "Listar OS em Andamento com Ordenação Prioritária",
            description = "Lista todas as OS em andamento (excluindo finalizadas e entregues) " +
                    "com ordenação prioritária: 1º EM_EXECUCAO, 2º AGUARDANDO_APROVACAO, " +
                    "3º EM_DIAGNOSTICO, 4º RECEBIDA. Dentro de cada status, as mais antigas primeiro."
    )
    public ResponseEntity<List<OrdemDeServicoResponseDTO>> listarEmAndamento() {
        List<OrdemDeServicoResponseDTO> response = listarOrdensServicoUseCase.executar();
        return ResponseEntity.ok(response);
    }
}

