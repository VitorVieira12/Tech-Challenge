package com.techchallenge.controller;

import com.techchallenge.domain.dto.ClienteDTO;
import com.techchallenge.domain.dto.ClienteResponseDTO;
import com.techchallenge.domain.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciamento de Clientes.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Cria um novo cliente.
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteDTO dto) {
        ClienteResponseDTO response = clienteService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um cliente por ID.
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteResponseDTO response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os clientes.
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> response = clienteService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um cliente existente.
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDTO dto) {
        ClienteResponseDTO response = clienteService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um cliente.
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

