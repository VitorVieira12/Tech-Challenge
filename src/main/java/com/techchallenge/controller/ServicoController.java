package com.techchallenge.controller;

import com.techchallenge.domain.dto.ServicoDTO;
import com.techchallenge.domain.dto.ServicoResponseDTO;
import com.techchallenge.domain.service.ServicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciamento de Serviços.
 */
@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "CRUD de serviços oferecidos pela oficina")
public class ServicoController {

    private final ServicoService servicoService;

    /**
     * Cria um novo serviço.
     * POST /api/servicos
     */
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> criar(@Valid @RequestBody ServicoDTO dto) {
        ServicoResponseDTO response = servicoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um serviço por ID.
     * GET /api/servicos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        ServicoResponseDTO response = servicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os serviços.
     * GET /api/servicos
     */
    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos() {
        List<ServicoResponseDTO> response = servicoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um serviço existente.
     * PUT /api/servicos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoDTO dto) {
        ServicoResponseDTO response = servicoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um serviço.
     * DELETE /api/servicos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

