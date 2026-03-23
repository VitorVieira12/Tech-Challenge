package com.techchallenge.adapters.gateway;

import com.techchallenge.adapters.persistence.VeiculoJpaEntity;
import com.techchallenge.adapters.persistence.VeiculoJpaRepository;
import com.techchallenge.core.domain.Veiculo;
import com.techchallenge.core.domain.valueobject.AnoVeiculo;
import com.techchallenge.core.domain.valueobject.Placa;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação do Gateway de Veiculo
 * Adapta a interface do domínio para o Spring Data JPA
 * (Adapter Pattern)
 */
@Component
public class VeiculoGatewayImpl implements VeiculoGateway {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoGatewayImpl(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        VeiculoJpaEntity entity = paraEntity(veiculo);
        VeiculoJpaEntity entitySalva = jpaRepository.save(entity);
        return paraDominio(entitySalva);
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::paraDominio);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(String placa) {
        return jpaRepository.findByPlaca(placa)
            .map(this::paraDominio);
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return jpaRepository.existsByPlaca(placa);
    }

    @Override
    public List<Veiculo> listarTodos() {
        return jpaRepository.findAll().stream()
            .map(this::paraDominio)
            .collect(Collectors.toList());
    }

    @Override
    public List<Veiculo> listarPorCliente(Long clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream()
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
    private Veiculo paraDominio(VeiculoJpaEntity entity) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(entity.getId());
        veiculo.setPlaca(new Placa(entity.getPlaca()));
        veiculo.setMarca(entity.getMarca());
        veiculo.setModelo(entity.getModelo());
        veiculo.setAno(new AnoVeiculo(entity.getAno()));
        veiculo.setClienteId(entity.getClienteId());
        return veiculo;
    }

    /**
     * Converte entidade de domínio para entidade JPA
     * (Evita vazamento do domínio)
     */
    private VeiculoJpaEntity paraEntity(Veiculo veiculo) {
        VeiculoJpaEntity entity = new VeiculoJpaEntity();
        entity.setId(veiculo.getId());
        entity.setPlaca(veiculo.getPlaca().getValor());
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno().getValor());
        entity.setClienteId(veiculo.getClienteId());
        return entity;
    }
}


