package com.techchallenge.domain.repository;

import com.techchallenge.domain.model.Cliente;
import com.techchallenge.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlaca(String placa);

    List<Veiculo> findByClienteId(Long clienteId);

    List<Veiculo> findByCliente(Cliente cliente);

    boolean existsByPlaca(String placa);
}



