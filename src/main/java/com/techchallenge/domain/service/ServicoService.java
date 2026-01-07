package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.ServicoDTO;
import com.techchallenge.domain.dto.ServicoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.Servico;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.valueobject.ValorMonetario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional
    public ServicoResponseDTO criar(ServicoDTO dto) {
        Servico servico = new Servico();
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(new ValorMonetario(dto.getPreco()));

        Servico servicoSalvo = servicoRepository.save(servico);
        return ServicoResponseDTO.fromEntity(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO buscarPorId(Long id) {
        Servico servico = servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        return ServicoResponseDTO.fromEntity(servico);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarTodos() {
        return servicoRepository.findAll().stream()
            .map(ServicoResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public ServicoResponseDTO atualizar(Long id, ServicoDTO dto) {
        Servico servico = servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));

        servico.setDescricao(dto.getDescricao());
        servico.setPreco(new ValorMonetario(dto.getPreco()));

        Servico servicoAtualizado = servicoRepository.save(servico);
        return ServicoResponseDTO.fromEntity(servicoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!servicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Serviço", id);
        }
        servicoRepository.deleteById(id);
    }
}

