package com.techchallenge.infrastructure.config;

import com.techchallenge.adapters.gateway.ClienteGatewayImpl;
import com.techchallenge.adapters.presenter.ClientePresenterImpl;
import com.techchallenge.core.usecase.cliente.*;
import com.techchallenge.core.usecase.cliente.gateway.ClienteGateway;
import com.techchallenge.core.usecase.cliente.presenter.ClientePresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de Beans para Casos de Uso de Cliente
 * 
 * Esta classe é responsável por:
 * 1. Instanciar os casos de uso
 * 2. Injetar dependências (Gateways e Presenters)
 * 3. Manter a camada Core livre de frameworks
 * 
 * Conforme feedback do professor: Controller NÃO instancia diretamente,
 * usa injeção de dependência através desta configuração
 */
@Configuration
public class ClienteUseCaseConfig {

    /**
     * Bean: Criar Cliente Use Case
     * Injeta Gateway e Presenter como dependências
     */
    @Bean
    public CriarClienteUseCase criarClienteUseCase(
            ClienteGateway clienteGateway,
            ClientePresenter clientePresenter) {
        return new CriarClienteUseCase(clienteGateway, clientePresenter);
    }

    /**
     * Bean: Buscar Cliente Use Case
     */
    @Bean
    public BuscarClienteUseCase buscarClienteUseCase(
            ClienteGateway clienteGateway,
            ClientePresenter clientePresenter) {
        return new BuscarClienteUseCase(clienteGateway, clientePresenter);
    }

    /**
     * Bean: Listar Clientes Use Case
     */
    @Bean
    public ListarClientesUseCase listarClientesUseCase(
            ClienteGateway clienteGateway,
            ClientePresenter clientePresenter) {
        return new ListarClientesUseCase(clienteGateway, clientePresenter);
    }

    /**
     * Bean: Atualizar Cliente Use Case
     */
    @Bean
    public AtualizarClienteUseCase atualizarClienteUseCase(
            ClienteGateway clienteGateway,
            ClientePresenter clientePresenter) {
        return new AtualizarClienteUseCase(clienteGateway, clientePresenter);
    }

    /**
     * Bean: Deletar Cliente Use Case
     */
    @Bean
    public DeletarClienteUseCase deletarClienteUseCase(ClienteGateway clienteGateway) {
        return new DeletarClienteUseCase(clienteGateway);
    }
}


