package com.techchallenge.controller;

import com.techchallenge.domain.dto.VeiculoDTO;
import com.techchallenge.domain.dto.VeiculoResponseDTO;
import com.techchallenge.domain.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciamento de Veículos.
 */
@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    /**
     * Cria um novo veículo.
     * POST /api/veiculos
     */
    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> criar(@Valid @RequestBody VeiculoDTO dto) {
        VeiculoResponseDTO response = veiculoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um veículo por ID.
     * GET /api/veiculos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        VeiculoResponseDTO response = veiculoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os veículos.
     * GET /api/veiculos
     */
    @GetMapping
    public ResponseEntity<List<VeiculoResponseDTO>> listarTodos(
            @RequestParam(required = false) Long clienteId) {
        
        List<VeiculoResponseDTO> response;
        if (clienteId != null) {
            response = veiculoService.listarPorCliente(clienteId);
        } else {
            response = veiculoService.listarTodos();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um veículo existente.
     * PUT /api/veiculos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VeiculoDTO dto) {
        VeiculoResponseDTO response = veiculoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um veículo.
     * DELETE /api/veiculos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        veiculoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

