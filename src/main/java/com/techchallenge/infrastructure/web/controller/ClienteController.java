package com.techchallenge.infrastructure.web.controller;

import com.techchallenge.core.usecase.cliente.*;
import com.techchallenge.core.usecase.cliente.dto.ClienteInputDTO;
import com.techchallenge.core.usecase.cliente.dto.ClienteOutputDTO;
import com.techchallenge.infrastructure.web.dto.ClienteRequestDTO;
import com.techchallenge.infrastructure.web.dto.ClienteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para Cliente
 * Responsável por:
 * 1. Receber requisições HTTP
 * 2. Validar dados de entrada
 * 3. Converter DTOs REST para DTOs de Use Case
 * 4. Invocar casos de uso
 * 5. Converter DTOs de Use Case para DTOs REST
 * 6. Retornar resposta HTTP
 */
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "CRUD de clientes")
public class ClienteController {

    private final CriarClienteUseCase criarClienteUseCase;
    private final BuscarClienteUseCase buscarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final DeletarClienteUseCase deletarClienteUseCase;

    /**
     * Controller instancia os casos de uso (Dependency Injection)
     * Conforme feedback do professor
     */
    public ClienteController(
            CriarClienteUseCase criarClienteUseCase,
            BuscarClienteUseCase buscarClienteUseCase,
            ListarClientesUseCase listarClientesUseCase,
            AtualizarClienteUseCase atualizarClienteUseCase,
            DeletarClienteUseCase deletarClienteUseCase) {
        this.criarClienteUseCase = criarClienteUseCase;
        this.buscarClienteUseCase = buscarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.deletarClienteUseCase = deletarClienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO request) {
        // 1. Converter DTO REST para DTO Use Case
        ClienteInputDTO input = new ClienteInputDTO(
            request.getNome(),
            request.getCpfCnpj(),
            request.getContato()
        );

        // 2. Executar caso de uso
        ClienteOutputDTO output = criarClienteUseCase.executar(input);

        // 3. Converter DTO Use Case para DTO REST
        ClienteResponseDTO response = ClienteResponseDTO.fromOutput(output);

        // 4. Retornar resposta HTTP
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteOutputDTO output = buscarClienteUseCase.executar(id);
        ClienteResponseDTO response = ClienteResponseDTO.fromOutput(output);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteOutputDTO> outputs = listarClientesUseCase.executar();
        List<ClienteResponseDTO> responses = outputs.stream()
            .map(ClienteResponseDTO::fromOutput)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO request) {
        
        ClienteInputDTO input = new ClienteInputDTO(
            request.getNome(),
            request.getCpfCnpj(),
            request.getContato()
        );

        ClienteOutputDTO output = atualizarClienteUseCase.executar(id, input);
        ClienteResponseDTO response = ClienteResponseDTO.fromOutput(output);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deletarClienteUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}

