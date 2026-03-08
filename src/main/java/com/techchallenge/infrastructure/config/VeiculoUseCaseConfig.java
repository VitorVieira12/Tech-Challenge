package com.techchallenge.infrastructure.config;

import com.techchallenge.adapters.gateway.VeiculoGatewayImpl;
import com.techchallenge.adapters.presenter.VeiculoPresenterImpl;
import com.techchallenge.core.usecase.veiculo.*;
import com.techchallenge.core.usecase.veiculo.gateway.VeiculoGateway;
import com.techchallenge.core.usecase.veiculo.presenter.VeiculoPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração dos casos de uso de Veiculo
 * Responsável por instanciar os use cases com suas dependências
 * (Dependency Injection conforme feedback do professor)
 */
@Configuration
public class VeiculoUseCaseConfig {

    /**
     * Gateway é um adaptador que será injetado automaticamente pelo Spring
     */
    private final VeiculoGateway veiculoGateway;
    
    /**
     * Presenter é um adaptador que será injetado automaticamente pelo Spring
     */
    private final VeiculoPresenter veiculoPresenter;

    public VeiculoUseCaseConfig(VeiculoGatewayImpl veiculoGateway, VeiculoPresenterImpl veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    @Bean
    public CriarVeiculoUseCase criarVeiculoUseCase() {
        return new CriarVeiculoUseCase(veiculoGateway, veiculoPresenter);
    }

    @Bean
    public BuscarVeiculoUseCase buscarVeiculoUseCase() {
        return new BuscarVeiculoUseCase(veiculoGateway, veiculoPresenter);
    }

    @Bean
    public ListarVeiculosUseCase listarVeiculosUseCase() {
        return new ListarVeiculosUseCase(veiculoGateway, veiculoPresenter);
    }

    @Bean
    public ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase() {
        return new ListarVeiculosPorClienteUseCase(veiculoGateway, veiculoPresenter);
    }

    @Bean
    public AtualizarVeiculoUseCase atualizarVeiculoUseCase() {
        return new AtualizarVeiculoUseCase(veiculoGateway, veiculoPresenter);
    }

    @Bean
    public DeletarVeiculoUseCase deletarVeiculoUseCase() {
        return new DeletarVeiculoUseCase(veiculoGateway);
    }
}


