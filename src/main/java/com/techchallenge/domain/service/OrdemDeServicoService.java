package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.MonitoramentoDTO;
import com.techchallenge.domain.dto.OrdemDeServicoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoPublicDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
import com.techchallenge.domain.dto.StatusUpdateDTO;
import com.techchallenge.domain.exception.EstoqueInsuficienteException;
import com.techchallenge.domain.exception.ResourceNotFoundException;
import com.techchallenge.domain.model.*;
import com.techchallenge.domain.repository.ClienteRepository;
import com.techchallenge.domain.repository.OrdemDeServicoRepository;
import com.techchallenge.domain.repository.PecaInsumoRepository;
import com.techchallenge.domain.repository.ServicoRepository;
import com.techchallenge.domain.repository.VeiculoRepository;
import com.techchallenge.domain.valueobject.AnoVeiculo;
import com.techchallenge.domain.valueobject.CpfCnpj;
import com.techchallenge.domain.valueobject.Placa;
import com.techchallenge.domain.valueobject.ValorMonetario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdemDeServicoService {

    private final OrdemDeServicoRepository ordemDeServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaInsumoRepository pecaInsumoRepository;

    @Transactional
    public OrdemDeServicoResponseDTO criarOS(OrdemDeServicoInputDTO dto) {
        log.info("Iniciando criação de OS para CPF/CNPJ: {}", dto.getCpfCnpjCliente());

        Cliente cliente = clienteRepository.findByCpfCnpjValor(dto.getCpfCnpjCliente())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente com CPF/CNPJ " + dto.getCpfCnpjCliente() + " não encontrado. " +
                "Por favor, cadastre o cliente antes de criar a ordem de serviço."
            ));
        
        log.info("Cliente encontrado: {} (ID: {})", cliente.getNome(), cliente.getId());

        Veiculo veiculo = obterOuCriarVeiculo(dto, cliente);
        log.info("Veículo identificado: {} (ID: {})", veiculo.getPlaca(), veiculo.getId());

        List<Servico> servicos = validarEBuscarServicos(dto);
        log.info("Serviços validados: {} itens", servicos.size());

        List<PecaInsumo> pecas = validarEstoqueEBuscarPecas(dto);
        log.info("Peças validadas: {} itens", pecas.size());

        OrdemDeServico os = new OrdemDeServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setObservacoes(dto.getObservacoes());
        os.setStatus(StatusOrdemServico.RECEBIDA);

        ValorMonetario valorTotal = new ValorMonetario(BigDecimal.ZERO);
        for (int i = 0; i < dto.getServicos().size(); i++) {
            var itemDTO = dto.getServicos().get(i);
            Servico servico = servicos.get(i);
            
            OrdemServicoItem item = new OrdemServicoItem();
            item.setServico(servico);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(servico.getPreco());
            item.setSubtotal(servico.getPreco().multiplicar(itemDTO.getQuantidade()));
            
            os.adicionarItemServico(item);
            valorTotal = valorTotal.somar(item.getSubtotal());
        }

        for (int i = 0; i < dto.getPecas().size(); i++) {
            var itemDTO = dto.getPecas().get(i);
            PecaInsumo peca = pecas.get(i);
            
            OrdemServicoPeca item = new OrdemServicoPeca();
            item.setPecaInsumo(peca);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(peca.getPreco());
            item.setSubtotal(peca.getPreco().multiplicar(itemDTO.getQuantidade()));
            
            os.adicionarItemPeca(item);
            valorTotal = valorTotal.somar(item.getSubtotal());

            peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() - itemDTO.getQuantidade());
            pecaInsumoRepository.save(peca);
        }

        os.setValorTotalOrcamento(valorTotal);

        OrdemDeServico osSalva = ordemDeServicoRepository.save(os);
        log.info("OS criada com sucesso. ID: {}, Valor Total: {}", osSalva.getId(), valorTotal);

        simularEnvioOrcamento(osSalva);

        return OrdemDeServicoResponseDTO.fromEntity(osSalva);
    }

    private Veiculo obterOuCriarVeiculo(OrdemDeServicoInputDTO dto, Cliente cliente) {
        Placa placa = new Placa(dto.getVeiculo().getPlaca());
        
        return veiculoRepository.findByPlacaValor(placa.getValor())
            .orElseGet(() -> {
                log.info("Veículo não encontrado. Cadastrando novo veículo com placa: {}", placa.getValor());

                if (dto.getVeiculo().getMarca() == null || dto.getVeiculo().getMarca().isBlank()) {
                    throw new IllegalArgumentException(
                        "Para cadastrar um novo veículo, a marca é obrigatória"
                    );
                }
                if (dto.getVeiculo().getModelo() == null || dto.getVeiculo().getModelo().isBlank()) {
                    throw new IllegalArgumentException(
                        "Para cadastrar um novo veículo, o modelo é obrigatório"
                    );
                }
                if (dto.getVeiculo().getAno() == null) {
                    throw new IllegalArgumentException(
                        "Para cadastrar um novo veículo, o ano é obrigatório"
                    );
                }
                
                Veiculo novoVeiculo = new Veiculo();
                novoVeiculo.setPlaca(placa);
                novoVeiculo.setMarca(dto.getVeiculo().getMarca());
                novoVeiculo.setModelo(dto.getVeiculo().getModelo());
                novoVeiculo.setAno(new AnoVeiculo(dto.getVeiculo().getAno()));
                novoVeiculo.setCliente(cliente);
                
                return veiculoRepository.save(novoVeiculo);
            });
    }

    private List<Servico> validarEBuscarServicos(OrdemDeServicoInputDTO dto) {
        List<Servico> servicos = new ArrayList<>();
        
        for (var itemDTO : dto.getServicos()) {
            Servico servico = servicoRepository.findById(itemDTO.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", itemDTO.getServicoId()));
            servicos.add(servico);
        }
        
        return servicos;
    }

    private List<PecaInsumo> validarEstoqueEBuscarPecas(OrdemDeServicoInputDTO dto) {
        List<PecaInsumo> pecas = new ArrayList<>();
        List<EstoqueInsuficienteException.ItemEstoqueInsuficiente> itensInsuficientes = new ArrayList<>();
        
        for (var itemDTO : dto.getPecas()) {
            PecaInsumo peca = pecaInsumoRepository.findById(itemDTO.getPecaInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo", itemDTO.getPecaInsumoId()));

            if (peca.getQuantidadeEstoque() < itemDTO.getQuantidade()) {
                itensInsuficientes.add(
                    new EstoqueInsuficienteException.ItemEstoqueInsuficiente(
                        peca.getId(),
                        peca.getNome(),
                        itemDTO.getQuantidade(),
                        peca.getQuantidadeEstoque()
                    )
                );
            }
            
            pecas.add(peca);
        }

        if (!itensInsuficientes.isEmpty()) {
            throw new EstoqueInsuficienteException(itensInsuficientes);
        }
        
        return pecas;
    }

    private void simularEnvioOrcamento(OrdemDeServico os) {
        log.info("=================================================");
        log.info("SIMULAÇÃO: Enviando orçamento ao cliente");
        log.info("Cliente: {} ({})", os.getCliente().getNome(), os.getCliente().getContato());
        log.info("Veículo: {} - {}", os.getVeiculo().getPlaca(), 
                 os.getVeiculo().getMarca() + " " + os.getVeiculo().getModelo());
        log.info("Valor Total: R$ {}", os.getValorTotalOrcamento());
        log.info("Status: Aguardando aprovação do cliente");
        log.info("=================================================");

        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordemDeServicoRepository.save(os);
    }

    @Transactional(readOnly = true)
    public OrdemDeServicoResponseDTO buscarPorId(Long id) {
        OrdemDeServico os = ordemDeServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", id));
        return OrdemDeServicoResponseDTO.fromEntity(os);
    }

    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarTodas() {
        return ordemDeServicoRepository.findAll().stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarPorStatus(StatusOrdemServico status) {
        return ordemDeServicoRepository.findByStatus(status).stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarPorCliente(Long clienteId) {
        return ordemDeServicoRepository.findByClienteId(clienteId).stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional
    public OrdemDeServicoResponseDTO atualizarStatus(Long id, StatusUpdateDTO statusUpdateDTO) {
        log.info("Atualizando status da OS {} para {}", id, statusUpdateDTO.getNovoStatus());
        
        OrdemDeServico os = ordemDeServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", id));
        
        StatusOrdemServico statusAtual = os.getStatus();
        StatusOrdemServico novoStatus = statusUpdateDTO.getNovoStatus();

        validarTransicaoStatus(statusAtual, novoStatus);

        os.setStatus(novoStatus);

        LocalDateTime agora = LocalDateTime.now();
        switch (novoStatus) {
            case EM_EXECUCAO:
                if (os.getDataInicioExecucao() == null) {
                    os.setDataInicioExecucao(agora);
                    log.info("Data de início de execução definida: {}", agora);
                }
                break;
            case FINALIZADA:
                if (os.getDataFinalizacao() == null) {
                    os.setDataFinalizacao(agora);
                    log.info("Data de finalização definida: {}", agora);
                }
                break;
            case ENTREGUE:
                if (os.getDataEntrega() == null) {
                    os.setDataEntrega(agora);
                    log.info("Data de entrega definida: {}", agora);
                }
                break;
        }

        if (statusUpdateDTO.getObservacao() != null && !statusUpdateDTO.getObservacao().isBlank()) {
            String observacaoAtual = os.getObservacoes() != null ? os.getObservacoes() : "";
            String novaObservacao = String.format("%s\n[%s] Status alterado para %s: %s", 
                observacaoAtual, agora, novoStatus, statusUpdateDTO.getObservacao());
            os.setObservacoes(novaObservacao.trim());
        }
        
        OrdemDeServico osSalva = ordemDeServicoRepository.save(os);
        log.info("Status da OS {} atualizado com sucesso de {} para {}", id, statusAtual, novoStatus);
        
        return OrdemDeServicoResponseDTO.fromEntity(osSalva);
    }

    private void validarTransicaoStatus(StatusOrdemServico statusAtual, StatusOrdemServico novoStatus) {
        Map<StatusOrdemServico, Set<StatusOrdemServico>> transicoesValidas = Map.of(
            StatusOrdemServico.RECEBIDA, EnumSet.of(
                StatusOrdemServico.EM_DIAGNOSTICO,
                StatusOrdemServico.AGUARDANDO_APROVACAO
            ),
            StatusOrdemServico.EM_DIAGNOSTICO, EnumSet.of(
                StatusOrdemServico.AGUARDANDO_APROVACAO,
                StatusOrdemServico.RECEBIDA
            ),
            StatusOrdemServico.AGUARDANDO_APROVACAO, EnumSet.of(
                StatusOrdemServico.EM_EXECUCAO,
                StatusOrdemServico.RECEBIDA
            ),
            StatusOrdemServico.EM_EXECUCAO, EnumSet.of(
                StatusOrdemServico.FINALIZADA,
                StatusOrdemServico.EM_DIAGNOSTICO
            ),
            StatusOrdemServico.FINALIZADA, EnumSet.of(
                StatusOrdemServico.ENTREGUE
            ),
            StatusOrdemServico.ENTREGUE, EnumSet.noneOf(StatusOrdemServico.class)
        );

        if (statusAtual == novoStatus) {
            log.warn("OS já está no status {}", novoStatus);
            return;
        }

        Set<StatusOrdemServico> statusPermitidos = transicoesValidas.get(statusAtual);
        if (statusPermitidos == null || !statusPermitidos.contains(novoStatus)) {
            throw new IllegalStateException(
                String.format("Transição de status inválida: não é possível mudar de %s para %s", 
                    statusAtual, novoStatus)
            );
        }
    }

    @Transactional(readOnly = true)
    public OrdemDeServicoPublicDTO consultarStatusPublico(Long osId, String cpfCnpjCliente) {
        log.info("Consulta pública da OS {} com CPF/CNPJ: {}", osId, cpfCnpjCliente);
        
        CpfCnpj cpfCnpj = new CpfCnpj(cpfCnpjCliente);
        
        OrdemDeServico os = ordemDeServicoRepository.findById(osId)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", osId));

        if (!os.getCliente().getCpfCnpj().equals(cpfCnpj)) {
            log.warn("Tentativa de acesso não autorizado à OS {} com CPF/CNPJ: {}", osId, cpfCnpjCliente);
            throw new ResourceNotFoundException("Ordem de Serviço", osId);
        }
        
        log.info("Consulta pública autorizada para OS {}", osId);
        return OrdemDeServicoPublicDTO.fromEntity(os);
    }

    @Transactional(readOnly = true)
    public MonitoramentoDTO calcularTempoMedioExecucao() {
        log.info("Calculando tempo médio de execução das OSs");

        List<OrdemDeServico> osFinalizadas = ordemDeServicoRepository.findByStatus(StatusOrdemServico.FINALIZADA);

        List<OrdemDeServico> osEntregues = ordemDeServicoRepository.findByStatus(StatusOrdemServico.ENTREGUE);
        List<OrdemDeServico> todasFinalizadas = new ArrayList<>();
        todasFinalizadas.addAll(osFinalizadas);
        todasFinalizadas.addAll(osEntregues);

        List<Duration> temposExecucao = todasFinalizadas.stream()
            .filter(os -> os.getDataInicioExecucao() != null && os.getDataFinalizacao() != null)
            .map(os -> Duration.between(os.getDataInicioExecucao(), os.getDataFinalizacao()))
            .toList();
        
        if (temposExecucao.isEmpty()) {
            log.info("Nenhuma OS finalizada com dados de tempo disponíveis");
            return new MonitoramentoDTO(0.0, 0L, 0.0, 0.0);
        }

        double mediaHoras = temposExecucao.stream()
            .mapToLong(Duration::toMinutes)
            .average()
            .orElse(0.0) / 60.0;
        
        double minimoHoras = temposExecucao.stream()
            .mapToLong(Duration::toMinutes)
            .min()
            .orElse(0) / 60.0;
        
        double maximoHoras = temposExecucao.stream()
            .mapToLong(Duration::toMinutes)
            .max()
            .orElse(0) / 60.0;
        
        long quantidade = temposExecucao.size();
        
        log.info("Tempo médio de execução calculado: {} horas (baseado em {} OSs)", 
            String.format("%.2f", mediaHoras), quantidade);
        
        return new MonitoramentoDTO(
            Math.round(mediaHoras * 100.0) / 100.0,  // Arredondar para 2 casas decimais
            quantidade,
            Math.round(minimoHoras * 100.0) / 100.0,
            Math.round(maximoHoras * 100.0) / 100.0
        );
    }
}

