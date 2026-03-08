package com.techchallenge.infrastructure.web.controller;

import com.techchallenge.core.usecase.veiculo.*;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoInputDTO;
import com.techchallenge.core.usecase.veiculo.dto.VeiculoOutputDTO;
import com.techchallenge.infrastructure.web.dto.VeiculoRequestDTO;
import com.techchallenge.infrastructure.web.dto.VeiculoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para Veiculo
 * Responsável por:
 * 1. Receber requisições HTTP
 * 2. Validar dados de entrada
 * 3. Converter DTOs REST para DTOs de Use Case
 * 4. Invocar casos de uso
 * 5. Converter DTOs de Use Case para DTOs REST
 * 6. Retornar resposta HTTP
 */
@RestController
@RequestMapping("/api/veiculos")
@Tag(name = "Veículos", description = "CRUD de veículos")
public class VeiculoController {

    private final CriarVeiculoUseCase criarVeiculoUseCase;
    private final BuscarVeiculoUseCase buscarVeiculoUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;
    private final ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final DeletarVeiculoUseCase deletarVeiculoUseCase;

    /**
     * Controller instancia os casos de uso (Dependency Injection)
     * Conforme feedback do professor
     */
    public VeiculoController(
            CriarVeiculoUseCase criarVeiculoUseCase,
            BuscarVeiculoUseCase buscarVeiculoUseCase,
            ListarVeiculosUseCase listarVeiculosUseCase,
            ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase,
            AtualizarVeiculoUseCase atualizarVeiculoUseCase,
            DeletarVeiculoUseCase deletarVeiculoUseCase) {
        this.criarVeiculoUseCase = criarVeiculoUseCase;
        this.buscarVeiculoUseCase = buscarVeiculoUseCase;
        this.listarVeiculosUseCase = listarVeiculosUseCase;
        this.listarVeiculosPorClienteUseCase = listarVeiculosPorClienteUseCase;
        this.atualizarVeiculoUseCase = atualizarVeiculoUseCase;
        this.deletarVeiculoUseCase = deletarVeiculoUseCase;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> criar(@Valid @RequestBody VeiculoRequestDTO request) {
        // 1. Converter DTO REST para DTO Use Case
        VeiculoInputDTO input = new VeiculoInputDTO(
            request.getPlaca(),
            request.getMarca(),
            request.getModelo(),
            request.getAno(),
            request.getClienteId()
        );

        // 2. Executar caso de uso
        VeiculoOutputDTO output = criarVeiculoUseCase.executar(input);

        // 3. Converter DTO Use Case para DTO REST
        VeiculoResponseDTO response = VeiculoResponseDTO.fromOutput(output);

        // 4. Retornar resposta HTTP
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        VeiculoOutputDTO output = buscarVeiculoUseCase.executar(id);
        VeiculoResponseDTO response = VeiculoResponseDTO.fromOutput(output);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VeiculoResponseDTO>> listar(
            @RequestParam(required = false) Long clienteId) {
        
        List<VeiculoOutputDTO> outputs;
        
        if (clienteId != null) {
            outputs = listarVeiculosPorClienteUseCase.executar(clienteId);
        } else {
            outputs = listarVeiculosUseCase.executar();
        }
        
        List<VeiculoResponseDTO> responses = outputs.stream()
            .map(VeiculoResponseDTO::fromOutput)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VeiculoRequestDTO request) {
        
        VeiculoInputDTO input = new VeiculoInputDTO(
            request.getPlaca(),
            request.getMarca(),
            request.getModelo(),
            request.getAno(),
            request.getClienteId()
        );

        VeiculoOutputDTO output = atualizarVeiculoUseCase.executar(id, input);
        VeiculoResponseDTO response = VeiculoResponseDTO.fromOutput(output);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarVeiculoUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}


