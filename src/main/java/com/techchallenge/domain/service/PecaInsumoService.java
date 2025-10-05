package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.PecaInsumoDTO;
import com.techchallenge.domain.dto.PecaInsumoResponseDTO;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.PecaInsumo;
import com.techchallenge.domain.repository.PecaInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de Peças e Insumos.
 */
@Service
@RequiredArgsConstructor
public class PecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    @Transactional
    public PecaInsumoResponseDTO criar(PecaInsumoDTO dto) {
        PecaInsumo pecaInsumo = new PecaInsumo();
        pecaInsumo.setNome(dto.getNome());
        pecaInsumo.setDescricao(dto.getDescricao());
        pecaInsumo.setPreco(dto.getPreco());
        pecaInsumo.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        PecaInsumo pecaSalva = pecaInsumoRepository.save(pecaInsumo);
        return PecaInsumoResponseDTO.fromEntity(pecaSalva);
    }

    @Transactional(readOnly = true)
    public PecaInsumoResponseDTO buscarPorId(Long id) {
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo", id));
        return PecaInsumoResponseDTO.fromEntity(pecaInsumo);
    }

    @Transactional(readOnly = true)
    public List<PecaInsumoResponseDTO> listarTodos() {
        return pecaInsumoRepository.findAll().stream()
            .map(PecaInsumoResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public PecaInsumoResponseDTO atualizar(Long id, PecaInsumoDTO dto) {
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo", id));

        pecaInsumo.setNome(dto.getNome());
        pecaInsumo.setDescricao(dto.getDescricao());
        pecaInsumo.setPreco(dto.getPreco());
        pecaInsumo.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        PecaInsumo pecaAtualizada = pecaInsumoRepository.save(pecaInsumo);
        return PecaInsumoResponseDTO.fromEntity(pecaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        if (!pecaInsumoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Peça/Insumo", id);
        }
        pecaInsumoRepository.deleteById(id);
    }

    @Transactional
    public PecaInsumoResponseDTO atualizarEstoque(Long id, Integer quantidadeAjuste) {
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo", id));

        int novaQuantidade = pecaInsumo.getQuantidadeEstoque() + quantidadeAjuste;
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        }

        pecaInsumo.setQuantidadeEstoque(novaQuantidade);
        PecaInsumo pecaAtualizada = pecaInsumoRepository.save(pecaInsumo);
        return PecaInsumoResponseDTO.fromEntity(pecaAtualizada);
    }
}

