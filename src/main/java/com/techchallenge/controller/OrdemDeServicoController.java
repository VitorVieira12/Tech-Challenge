package com.techchallenge.controller;

import com.techchallenge.domain.dto.MonitoramentoDTO;
import com.techchallenge.domain.dto.OrdemDeServicoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoPublicDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.dto.StatusUpdateDTO;
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

    /**
     * Atualiza o status de uma Ordem de Serviço (uso administrativo).
     * PATCH /api/ordens-servico/{id}/status
     * 
     * Valida as transições de status conforme regras de negócio.
     * Exemplos de transições válidas:
     * - RECEBIDA -> EM_DIAGNOSTICO ou AGUARDANDO_APROVACAO
     * - AGUARDANDO_APROVACAO -> EM_EXECUCAO
     * - EM_EXECUCAO -> FINALIZADA
     * - FINALIZADA -> ENTREGUE (não pode voltar)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemDeServicoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusUpdateDTO) {
        
        OrdemDeServicoResponseDTO response = ordemDeServicoService.atualizarStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta pública do status de uma OS para o cliente.
     * GET /api/ordens-servico/status/{id}
     * 
     * Endpoint público que requer autenticação via CPF/CNPJ do cliente.
     * Retorna apenas informações essenciais e seguras.
     * 
     * Query Parameter obrigatório:
     * - cpfCnpj: CPF/CNPJ do cliente para autenticação
     */
    @GetMapping("/status/{id}")
    public ResponseEntity<OrdemDeServicoPublicDTO> consultarStatusPublico(
            @PathVariable Long id,
            @RequestParam String cpfCnpj) {
        
        OrdemDeServicoPublicDTO response = ordemDeServicoService.consultarStatusPublico(id, cpfCnpj);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna estatísticas de monitoramento das Ordens de Serviço.
     * GET /api/ordens-servico/monitoramento/tempo-medio
     * 
     * Calcula o tempo médio de execução das OSs finalizadas,
     * considerando o período entre o início da execução e a finalização.
     */
    @GetMapping("/monitoramento/tempo-medio")
    public ResponseEntity<MonitoramentoDTO> consultarTempoMedio() {
        MonitoramentoDTO response = ordemDeServicoService.calcularTempoMedioExecucao();
        return ResponseEntity.ok(response);
    }
}

