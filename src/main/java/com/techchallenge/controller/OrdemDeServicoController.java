package com.techchallenge.controller;

import com.techchallenge.domain.dto.OrdemDeServicoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.model.StatusOrdemServico;
import com.techchallenge.domain.service.OrdemDeServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciamento de Ordens de Serviço.
 */
@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
public class OrdemDeServicoController {

    private final OrdemDeServicoService ordemDeServicoService;

    /**
     * Cria uma nova Ordem de Serviço.
     * POST /api/ordens-servico
     * 
     * Este endpoint:
     * 1. Identifica o cliente pelo CPF/CNPJ
     * 2. Verifica/cadastra o veículo
     * 3. Valida estoque de peças
     * 4. Gera orçamento automático
     * 5. Cria a OS com status RECEBIDA
     * 6. Simula envio do orçamento (muda para AGUARDANDO_APROVACAO)
     */
    @PostMapping
    public ResponseEntity<OrdemDeServicoResponseDTO> criar(@Valid @RequestBody OrdemDeServicoInputDTO dto) {
        OrdemDeServicoResponseDTO response = ordemDeServicoService.criarOS(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca uma Ordem de Serviço por ID.
     * GET /api/ordens-servico/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdemDeServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        OrdemDeServicoResponseDTO response = ordemDeServicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas as Ordens de Serviço.
     * GET /api/ordens-servico
     * 
     * Query Parameters opcionais:
     * - status: filtra por status (RECEBIDA, AGUARDANDO_APROVACAO, etc.)
     * - clienteId: filtra por cliente
     */
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
}

