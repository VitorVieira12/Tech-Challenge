package com.techchallenge.domain.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EstoqueInsuficienteException extends RuntimeException {
    
    private final List<ItemEstoqueInsuficiente> itensInsuficientes;

    public EstoqueInsuficienteException(String message, List<ItemEstoqueInsuficiente> itensInsuficientes) {
        super(message);
        this.itensInsuficientes = itensInsuficientes;
    }

    public EstoqueInsuficienteException(List<ItemEstoqueInsuficiente> itensInsuficientes) {
        super(buildMessage(itensInsuficientes));
        this.itensInsuficientes = itensInsuficientes;
    }

    private static String buildMessage(List<ItemEstoqueInsuficiente> itens) {
        if (itens.isEmpty()) {
            return "Estoque insuficiente";
        }
        
        StringBuilder sb = new StringBuilder("Estoque insuficiente para as seguintes peças: ");
        for (int i = 0; i < itens.size(); i++) {
            ItemEstoqueInsuficiente item = itens.get(i);
            sb.append(item.getNomePeca())
              .append(" (solicitado: ")
              .append(item.getQuantidadeSolicitada())
              .append(", disponível: ")
              .append(item.getQuantidadeDisponivel())
              .append(")");
            if (i < itens.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Getter
    public static class ItemEstoqueInsuficiente {
        private final Long pecaId;
        private final String nomePeca;
        private final Integer quantidadeSolicitada;
        private final Integer quantidadeDisponivel;

        public ItemEstoqueInsuficiente(Long pecaId, String nomePeca, Integer quantidadeSolicitada, Integer quantidadeDisponivel) {
            this.pecaId = pecaId;
            this.nomePeca = nomePeca;
            this.quantidadeSolicitada = quantidadeSolicitada;
            this.quantidadeDisponivel = quantidadeDisponivel;
        }
    }
}

