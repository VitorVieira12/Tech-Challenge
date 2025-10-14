package com.techchallenge.controller;

import com.techchallenge.domain.dto.PecaInsumoDTO;
import com.techchallenge.domain.dto.PecaInsumoResponseDTO;
import com.techchallenge.domain.service.PecaInsumoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciamento de Peças e Insumos.
 */
@RestController
@RequestMapping("/api/pecas-insumos")
@RequiredArgsConstructor
@Tag(name = "Peças e Insumos", description = "CRUD de peças e insumos com gestão de estoque")
public class PecaInsumoController {

    private final PecaInsumoService pecaInsumoService;

    /**
     * Cria uma nova peça/insumo.
     * POST /api/pecas-insumos
     */
    @PostMapping
    public ResponseEntity<PecaInsumoResponseDTO> criar(@Valid @RequestBody PecaInsumoDTO dto) {
        PecaInsumoResponseDTO response = pecaInsumoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca uma peça/insumo por ID.
     * GET /api/pecas-insumos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PecaInsumoResponseDTO> buscarPorId(@PathVariable Long id) {
        PecaInsumoResponseDTO response = pecaInsumoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas as peças/insumos.
     * GET /api/pecas-insumos
     */
    @GetMapping
    public ResponseEntity<List<PecaInsumoResponseDTO>> listarTodos() {
        List<PecaInsumoResponseDTO> response = pecaInsumoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza uma peça/insumo existente.
     * PUT /api/pecas-insumos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PecaInsumoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PecaInsumoDTO dto) {
        PecaInsumoResponseDTO response = pecaInsumoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza o estoque de uma peça/insumo.
     * PATCH /api/pecas-insumos/{id}/estoque
     */
    @PatchMapping("/{id}/estoque")
    public ResponseEntity<PecaInsumoResponseDTO> atualizarEstoque(
            @PathVariable Long id,
            @RequestParam Integer quantidadeAjuste) {
        PecaInsumoResponseDTO response = pecaInsumoService.atualizarEstoque(id, quantidadeAjuste);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta uma peça/insumo.
     * DELETE /api/pecas-insumos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pecaInsumoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

