package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.ClienteDTO;
import com.techchallenge.domain.dto.ClienteResponseDTO;
import com.techchallenge.domain.exception.DuplicateResourceException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO criar(ClienteDTO dto) {
        CpfCnpj cpfCnpj = new CpfCnpj(dto.getCpfCnpj());
        
        if (clienteRepository.existsByCpfCnpjValor(cpfCnpj.getValor())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com este CPF/CNPJ");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setContato(new Contato(dto.getContato()));

        Cliente clienteSalvo = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(clienteSalvo);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return ClienteResponseDTO.fromEntity(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream()
            .map(ClienteResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        CpfCnpj novoCpfCnpj = new CpfCnpj(dto.getCpfCnpj());
        
        if (!cliente.getCpfCnpj().getValor().equals(novoCpfCnpj.getValor()) && 
            clienteRepository.existsByCpfCnpjValor(novoCpfCnpj.getValor())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com este CPF/CNPJ");
        }

        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(novoCpfCnpj);
        cliente.setContato(new Contato(dto.getContato()));

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(clienteAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clienteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}

