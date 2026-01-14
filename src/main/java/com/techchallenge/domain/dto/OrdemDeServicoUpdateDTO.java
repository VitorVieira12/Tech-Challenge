package com.techchallenge.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de informações da Ordem de Serviço
 * Permite atualizar apenas em status editáveis: RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, EM_EXECUCAO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoUpdateDTO {
    
    /**
     * Observações sobre a OS (opcional)
     * Pode conter diagnósticos, problemas identificados, etc.
     */
    @Size(max = 1000, message = "As observações não podem ter mais de 1000 caracteres")
    private String observacoes;
    
    /**
     * Nota: Por enquanto permitimos apenas atualizar observações.
     * No futuro, podemos adicionar campos para atualizar serviços e peças
     * se houver necessidade.
     */
}

