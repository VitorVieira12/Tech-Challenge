package com.techchallenge.adapters.gateway;

import com.techchallenge.adapters.persistence.ClienteJpaEntity;
import com.techchallenge.adapters.persistence.ClienteJpaRepository;
import com.techchallenge.core.domain.Cliente;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação do Gateway de Cliente
 * Adapta a interface do domínio para o Spring Data JPA
 * (Adapter Pattern)
 */
@Component
public class ClienteGatewayImpl implements ClienteGateway {

    private final ClienteJpaRepository jpaRepository;

    public ClienteGatewayImpl(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteJpaEntity entity = paraEntity(cliente);
        ClienteJpaEntity entitySalva = jpaRepository.save(entity);
        return paraDominio(entitySalva);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorCpfCnpj(String cpfCnpj) {
        return jpaRepository.findByCpfCnpj(cpfCnpj)
            .map(this::paraDominio);
    }

    @Override
    public boolean existePorCpfCnpj(String cpfCnpj) {
        return jpaRepository.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpaRepository.findAll().stream()
            .map(this::paraDominio)
            .collect(Collectors.toList());
    }

    @Override
    public void deletar(Long id) {
        jpaRepository.deleteById(id);
    }

    /**
     * Converte entidade JPA para entidade de domínio
     * (Evita vazamento do domínio)
     */
    private Cliente paraDominio(ClienteJpaEntity entity) {
        Cliente cliente = new Cliente();
        cliente.setId(entity.getId());
        cliente.setNome(entity.getNome());
        cliente.setCpfCnpj(new com.techchallenge.core.domain.valueobject.CpfCnpj(entity.getCpfCnpj()));
        cliente.setContato(new com.techchallenge.core.domain.valueobject.Contato(entity.getContato()));
        cliente.setAtivo(entity.isAtivo());
        return cliente;
    }

    /**
     * Converte entidade de domínio para entidade JPA
     * (Evita vazamento do domínio)
     */
    private ClienteJpaEntity paraEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.getId());
        entity.setNome(cliente.getNome());
        entity.setCpfCnpj(cliente.getCpfCnpj().getValor());
        entity.setContato(cliente.getContato().getValor());
        entity.setAtivo(cliente.isAtivo());
        return entity;
    }
}

