package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.VeiculoDTO;
import com.techchallenge.domain.dto.VeiculoResponseDTO;
import com.techchallenge.domain.exception.DuplicateResourceException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.Veiculo;
import com.techchallenge.domain.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de Veículos.
 */
@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;

    @Transactional
    public VeiculoResponseDTO criar(VeiculoDTO dto) {
        // Verifica se já existe veículo com a mesma placa
        if (veiculoRepository.existsByPlaca(dto.getPlaca())) {
            throw new DuplicateResourceException("Já existe um veículo cadastrado com esta placa");
        }

        // Busca o cliente
        Cliente cliente = clienteService.buscarEntidadePorId(dto.getClienteId());

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(dto.getPlaca().toUpperCase());
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAno(dto.getAno());
        veiculo.setCliente(cliente);

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);
        return VeiculoResponseDTO.fromEntity(veiculoSalvo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponseDTO buscarPorId(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));
        return VeiculoResponseDTO.fromEntity(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarTodos() {
        return veiculoRepository.findAll().stream()
            .map(VeiculoResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarPorCliente(Long clienteId) {
        Cliente cliente = clienteService.buscarEntidadePorId(clienteId);
        return veiculoRepository.findByCliente(cliente).stream()
            .map(VeiculoResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public VeiculoResponseDTO atualizar(Long id, VeiculoDTO dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));

        // Verifica se a placa já está em uso por outro veículo
        if (!veiculo.getPlaca().equals(dto.getPlaca().toUpperCase()) && 
            veiculoRepository.existsByPlaca(dto.getPlaca())) {
            throw new DuplicateResourceException("Já existe um veículo cadastrado com esta placa");
        }

        // Busca o cliente
        Cliente cliente = clienteService.buscarEntidadePorId(dto.getClienteId());

        veiculo.setPlaca(dto.getPlaca().toUpperCase());
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAno(dto.getAno());
        veiculo.setCliente(cliente);

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);
        return VeiculoResponseDTO.fromEntity(veiculoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!veiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Veículo", id);
        }
        veiculoRepository.deleteById(id);
    }
}

