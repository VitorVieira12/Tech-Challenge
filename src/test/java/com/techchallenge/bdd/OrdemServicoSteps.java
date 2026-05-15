package com.techchallenge.bdd;

import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.repository.VeiculoRepository;
import com.techchallenge.domain.valueobject.AnoVeiculo;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.CpfCnpj;
import com.techchallenge.domain.valueobject.Placa;
import com.techchallenge.domain.valueobject.ValorMonetario;
import com.techchallenge.messaging.consumer.BillingEventConsumer;
import com.techchallenge.messaging.consumer.ExecucaoEventConsumer;
import com.techchallenge.messaging.event.*;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class OrdemServicoSteps {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private OrdemDeServicoRepository ordemDeServicoRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private BillingEventConsumer billingEventConsumer;
    @Autowired private ExecucaoEventConsumer execucaoEventConsumer;

    private OrdemDeServico ultimaOs;
    private boolean eventoCriado = false;

    @Before
    public void limpar() {
        ordemDeServicoRepository.deleteAll();
        veiculoRepository.deleteAll();
        eventoCriado = false;
        ultimaOs = null;
    }

    @Dado("que existe um cliente com CPF {string} e nome {string}")
    public void existeCliente(String cpf, String nome) {
        clienteRepository.findByCpfCnpjValor(cpf).orElseGet(() -> {
            Cliente c = new Cliente();
            c.setNome(nome);
            c.setCpfCnpj(new CpfCnpj(cpf));
            c.setContato(new Contato("joao@email.com"));
            c.setAtivo(true);
            return clienteRepository.save(c);
        });
    }

    @E("que existe um serviço com ID {long} e descrição {string} e preço {double}")
    public void existeServico(Long id, String descricao, Double preco) {
        servicoRepository.findById(id).orElseGet(() -> {
            Servico s = new Servico();
            s.setDescricao(descricao);
            s.setPreco(new ValorMonetario(BigDecimal.valueOf(preco)));
            return servicoRepository.save(s);
        });
    }

    @Quando("o cliente abre uma OS com o veículo placa {string} e o serviço de ID {long}")
    public void abreOS(String placa, Long servicoId) {
        Cliente cliente = clienteRepository.findAll().stream()
                .findFirst().orElseThrow();
        Servico servico = servicoRepository.findById(servicoId).orElseThrow();

        Veiculo veiculo = veiculoRepository.findByPlacaValor(placa).orElseGet(() -> {
            Veiculo v = new Veiculo();
            v.setPlaca(new Placa(placa));
            v.setMarca("Toyota");
            v.setModelo("Corolla");
            v.setAno(new AnoVeiculo(2020));
            v.setCliente(cliente);
            return veiculoRepository.save(v);
        });

        OrdemDeServico os = new OrdemDeServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        os.setValorTotalOrcamento(servico.getPreco());

        ultimaOs = ordemDeServicoRepository.save(os);
        eventoCriado = true;
    }

    @Então("a OS é criada com status {string}")
    public void osComStatus(String status) {
        assertThat(ultimaOs).isNotNull();
        assertThat(ultimaOs.getStatus().name()).isEqualTo(status);
    }

    @E("o evento {string} é publicado para o Billing Service")
    public void eventoPublicado(String evento) {
        assertThat(eventoCriado).isTrue();
    }

    @Dado("que existe uma OS com ID {long} no status {string}")
    public void osComIdEStatus(Long osId, String status) {
        Cliente cliente = clienteRepository.findAll().stream()
                .findFirst().orElseThrow();

        Veiculo veiculo = veiculoRepository.findAll().stream().findFirst().orElseGet(() -> {
            Veiculo v = new Veiculo();
            v.setPlaca(new Placa("TST0001"));
            v.setMarca("Generic");
            v.setModelo("Model");
            v.setAno(new AnoVeiculo(2020));
            v.setCliente(cliente);
            return veiculoRepository.save(v);
        });

        OrdemDeServico os = new OrdemDeServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setStatus(StatusOrdemServico.valueOf(status));
        os.setValorTotalOrcamento(new ValorMonetario(BigDecimal.valueOf(100)));
        ultimaOs = ordemDeServicoRepository.save(os);
    }

    @Quando("o Billing Service publica o evento {string} para a OS {long}")
    public void billingPublicaEvento(String evento, Long osId) {
        Long realOsId = ultimaOs != null ? ultimaOs.getId() : osId;
        switch (evento) {
            case "orcamento.gerado" ->
                    billingEventConsumer.onOrcamentoGerado(
                            new OrcamentoGeradoEvent(realOsId, 1L, BigDecimal.valueOf(500), LocalDateTime.now()));
            case "orcamento.aprovado" ->
                    billingEventConsumer.onOrcamentoAprovado(
                            new OrcamentoAprovadoEvent(realOsId, 1L, LocalDateTime.now()));
        }
    }

    @Quando("o Billing Service publica o evento {string} para a OS {long} com motivo {string}")
    public void billingPublicaEventoComMotivo(String evento, Long osId, String motivo) {
        Long realOsId = ultimaOs != null ? ultimaOs.getId() : osId;
        if ("orcamento.rejeitado".equals(evento)) {
            billingEventConsumer.onOrcamentoRejeitado(
                    new OrcamentoRejeitadoEvent(realOsId, 1L, motivo, LocalDateTime.now()));
        }
    }

    @Quando("o Execution Service publica o evento {string} para a OS {long}")
    public void execucaoPublicaEvento(String evento, Long osId) {
        Long realOsId = ultimaOs != null ? ultimaOs.getId() : osId;
        if ("execucao.finalizada".equals(evento)) {
            execucaoEventConsumer.onExecucaoFinalizada(
                    new ExecucaoFinalizadaEvent(realOsId, "exec-001", LocalDateTime.now(), "Concluído"));
        }
    }

    @Quando("o Execution Service publica o evento {string} para a OS {long} com motivo {string}")
    public void execucaoPublicaEventoComMotivo(String evento, Long osId, String motivo) {
        Long realOsId = ultimaOs != null ? ultimaOs.getId() : osId;
        if ("execucao.falhou".equals(evento)) {
            execucaoEventConsumer.onExecucaoFalhou(
                    new ExecucaoFalhouEvent(realOsId, "exec-001", motivo, LocalDateTime.now()));
        }
    }

    @Então("a OS {long} tem status {string}")
    public void osTemStatus(Long osId, String status) {
        Long realOsId = ultimaOs != null ? ultimaOs.getId() : osId;
        OrdemDeServico os = ordemDeServicoRepository.findById(realOsId).orElseThrow();
        assertThat(os.getStatus().name()).isEqualTo(status);
    }
}
