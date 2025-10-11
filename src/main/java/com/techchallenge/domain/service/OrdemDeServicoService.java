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

/**
 * Serviço para gerenciamento de Ordens de Serviço.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdemDeServicoService {

    private final OrdemDeServicoRepository ordemDeServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaInsumoRepository pecaInsumoRepository;

    /**
     * Cria uma nova Ordem de Serviço.
     * 
     * @param dto Dados da OS a ser criada
     * @return DTO com os dados da OS criada
     * @throws ResourceNotFoundException se cliente não encontrado
     * @throws EstoqueInsuficienteException se alguma peça não tiver estoque suficiente
     */
    @Transactional
    public OrdemDeServicoResponseDTO criarOS(OrdemDeServicoInputDTO dto) {
        log.info("Iniciando criação de OS para CPF/CNPJ: {}", dto.getCpfCnpjCliente());

        // Passo 1: Identificar o cliente pelo CPF/CNPJ
        Cliente cliente = clienteRepository.findByCpfCnpj(dto.getCpfCnpjCliente())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente com CPF/CNPJ " + dto.getCpfCnpjCliente() + " não encontrado. " +
                "Por favor, cadastre o cliente antes de criar a ordem de serviço."
            ));
        
        log.info("Cliente encontrado: {} (ID: {})", cliente.getNome(), cliente.getId());

        // Passo 2: Verificar/Cadastrar veículo
        Veiculo veiculo = obterOuCriarVeiculo(dto, cliente);
        log.info("Veículo identificado: {} (ID: {})", veiculo.getPlaca(), veiculo.getId());

        // Passo 3: Validar e buscar serviços
        List<Servico> servicos = validarEBuscarServicos(dto);
        log.info("Serviços validados: {} itens", servicos.size());

        // Passo 4: Validar estoque e buscar peças
        List<PecaInsumo> pecas = validarEstoqueEBuscarPecas(dto);
        log.info("Peças validadas: {} itens", pecas.size());

        // Passo 5: Criar a Ordem de Serviço
        OrdemDeServico os = new OrdemDeServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setObservacoes(dto.getObservacoes());
        os.setStatus(StatusOrdemServico.RECEBIDA);

        // Adicionar itens de serviço
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (int i = 0; i < dto.getServicos().size(); i++) {
            var itemDTO = dto.getServicos().get(i);
            Servico servico = servicos.get(i);
            
            OrdemServicoItem item = new OrdemServicoItem();
            item.setServico(servico);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(servico.getPreco());
            item.setSubtotal(servico.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));
            
            os.adicionarItemServico(item);
            valorTotal = valorTotal.add(item.getSubtotal());
        }

        // Adicionar itens de peça e baixar estoque
        for (int i = 0; i < dto.getPecas().size(); i++) {
            var itemDTO = dto.getPecas().get(i);
            PecaInsumo peca = pecas.get(i);
            
            OrdemServicoPeca item = new OrdemServicoPeca();
            item.setPecaInsumo(peca);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(peca.getPreco());
            item.setSubtotal(peca.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));
            
            os.adicionarItemPeca(item);
            valorTotal = valorTotal.add(item.getSubtotal());

            // Baixar do estoque
            peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() - itemDTO.getQuantidade());
            pecaInsumoRepository.save(peca);
        }

        os.setValorTotalOrcamento(valorTotal);
        
        // Salvar a OS
        OrdemDeServico osSalva = ordemDeServicoRepository.save(os);
        log.info("OS criada com sucesso. ID: {}, Valor Total: {}", osSalva.getId(), valorTotal);

        // Passo 6: Simular envio de orçamento
        simularEnvioOrcamento(osSalva);

        return OrdemDeServicoResponseDTO.fromEntity(osSalva);
    }

    /**
     * Obtém veículo existente ou cria um novo.
     */
    private Veiculo obterOuCriarVeiculo(OrdemDeServicoInputDTO dto, Cliente cliente) {
        String placa = dto.getVeiculo().getPlaca().toUpperCase();
        
        return veiculoRepository.findByPlaca(placa)
            .orElseGet(() -> {
                log.info("Veículo não encontrado. Cadastrando novo veículo com placa: {}", placa);
                
                // Validar que os dados completos do veículo foram informados
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
                novoVeiculo.setAno(dto.getVeiculo().getAno());
                novoVeiculo.setCliente(cliente);
                
                return veiculoRepository.save(novoVeiculo);
            });
    }

    /**
     * Valida e busca os serviços solicitados.
     */
    private List<Servico> validarEBuscarServicos(OrdemDeServicoInputDTO dto) {
        List<Servico> servicos = new ArrayList<>();
        
        for (var itemDTO : dto.getServicos()) {
            Servico servico = servicoRepository.findById(itemDTO.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", itemDTO.getServicoId()));
            servicos.add(servico);
        }
        
        return servicos;
    }

    /**
     * Valida estoque e busca as peças solicitadas.
     * Lança exceção se alguma peça não tiver estoque suficiente.
     */
    private List<PecaInsumo> validarEstoqueEBuscarPecas(OrdemDeServicoInputDTO dto) {
        List<PecaInsumo> pecas = new ArrayList<>();
        List<EstoqueInsuficienteException.ItemEstoqueInsuficiente> itensInsuficientes = new ArrayList<>();
        
        for (var itemDTO : dto.getPecas()) {
            PecaInsumo peca = pecaInsumoRepository.findById(itemDTO.getPecaInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Peça/Insumo", itemDTO.getPecaInsumoId()));
            
            // Verificar estoque
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
        
        // Se houver itens com estoque insuficiente, lançar exceção
        if (!itensInsuficientes.isEmpty()) {
            throw new EstoqueInsuficienteException(itensInsuficientes);
        }
        
        return pecas;
    }

    /**
     * Simula o envio do orçamento ao cliente.
     * Em produção, isso poderia enviar email, SMS, WhatsApp, etc.
     */
    private void simularEnvioOrcamento(OrdemDeServico os) {
        log.info("=================================================");
        log.info("SIMULAÇÃO: Enviando orçamento ao cliente");
        log.info("Cliente: {} ({})", os.getCliente().getNome(), os.getCliente().getContato());
        log.info("Veículo: {} - {}", os.getVeiculo().getPlaca(), 
                 os.getVeiculo().getMarca() + " " + os.getVeiculo().getModelo());
        log.info("Valor Total: R$ {}", os.getValorTotalOrcamento());
        log.info("Status: Aguardando aprovação do cliente");
        log.info("=================================================");
        
        // Atualizar status para aguardando aprovação
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordemDeServicoRepository.save(os);
    }

    /**
     * Busca uma OS por ID.
     */
    @Transactional(readOnly = true)
    public OrdemDeServicoResponseDTO buscarPorId(Long id) {
        OrdemDeServico os = ordemDeServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", id));
        return OrdemDeServicoResponseDTO.fromEntity(os);
    }

    /**
     * Lista todas as OSs.
     */
    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarTodas() {
        return ordemDeServicoRepository.findAll().stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    /**
     * Lista OSs por status.
     */
    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarPorStatus(StatusOrdemServico status) {
        return ordemDeServicoRepository.findByStatus(status).stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    /**
     * Lista OSs de um cliente.
     */
    @Transactional(readOnly = true)
    public List<OrdemDeServicoResponseDTO> listarPorCliente(Long clienteId) {
        return ordemDeServicoRepository.findByClienteId(clienteId).stream()
            .map(OrdemDeServicoResponseDTO::fromEntity)
            .toList();
    }

    /**
     * Atualiza o status de uma Ordem de Serviço.
     * Valida as transições de status e atualiza as datas correspondentes.
     * 
     * @param id ID da OS
     * @param statusUpdateDTO Dados da atualização de status
     * @return DTO com os dados atualizados da OS
     * @throws ResourceNotFoundException se OS não encontrada
     * @throws IllegalStateException se transição de status inválida
     */
    @Transactional
    public OrdemDeServicoResponseDTO atualizarStatus(Long id, StatusUpdateDTO statusUpdateDTO) {
        log.info("Atualizando status da OS {} para {}", id, statusUpdateDTO.getNovoStatus());
        
        OrdemDeServico os = ordemDeServicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", id));
        
        StatusOrdemServico statusAtual = os.getStatus();
        StatusOrdemServico novoStatus = statusUpdateDTO.getNovoStatus();
        
        // Validar transição de status
        validarTransicaoStatus(statusAtual, novoStatus);
        
        // Atualizar status
        os.setStatus(novoStatus);
        
        // Atualizar datas conforme o novo status
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
        
        // Adicionar observação se fornecida
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

    /**
     * Valida se a transição de status é permitida.
     * Define as regras de negócio para mudanças de status.
     * 
     * @param statusAtual Status atual da OS
     * @param novoStatus Novo status desejado
     * @throws IllegalStateException se a transição não for permitida
     */
    private void validarTransicaoStatus(StatusOrdemServico statusAtual, StatusOrdemServico novoStatus) {
        // Mapear as transições válidas para cada status
        Map<StatusOrdemServico, Set<StatusOrdemServico>> transicoesValidas = Map.of(
            StatusOrdemServico.RECEBIDA, EnumSet.of(
                StatusOrdemServico.EM_DIAGNOSTICO,
                StatusOrdemServico.AGUARDANDO_APROVACAO
            ),
            StatusOrdemServico.EM_DIAGNOSTICO, EnumSet.of(
                StatusOrdemServico.AGUARDANDO_APROVACAO,
                StatusOrdemServico.RECEBIDA  // Pode voltar se necessário
            ),
            StatusOrdemServico.AGUARDANDO_APROVACAO, EnumSet.of(
                StatusOrdemServico.EM_EXECUCAO,
                StatusOrdemServico.RECEBIDA  // Cliente pode rejeitar e voltar
            ),
            StatusOrdemServico.EM_EXECUCAO, EnumSet.of(
                StatusOrdemServico.FINALIZADA,
                StatusOrdemServico.EM_DIAGNOSTICO  // Pode voltar se houver problema
            ),
            StatusOrdemServico.FINALIZADA, EnumSet.of(
                StatusOrdemServico.ENTREGUE
                // Não permite voltar de FINALIZADA para outros status
            ),
            StatusOrdemServico.ENTREGUE, EnumSet.noneOf(StatusOrdemServico.class)
            // ENTREGUE é o estado final, não permite alterações
        );
        
        // Se já está no status desejado, não fazer nada
        if (statusAtual == novoStatus) {
            log.warn("OS já está no status {}", novoStatus);
            return;
        }
        
        // Verificar se a transição é válida
        Set<StatusOrdemServico> statusPermitidos = transicoesValidas.get(statusAtual);
        if (statusPermitidos == null || !statusPermitidos.contains(novoStatus)) {
            throw new IllegalStateException(
                String.format("Transição de status inválida: não é possível mudar de %s para %s", 
                    statusAtual, novoStatus)
            );
        }
    }

    /**
     * Consulta pública de OS para o cliente.
     * Requer autenticação via CPF/CNPJ + ID da OS.
     * Retorna apenas informações essenciais e seguras.
     * 
     * @param osId ID da Ordem de Serviço
     * @param cpfCnpjCliente CPF/CNPJ do cliente para autenticação
     * @return DTO público com informações essenciais da OS
     * @throws ResourceNotFoundException se OS não encontrada ou não pertence ao cliente
     */
    @Transactional(readOnly = true)
    public OrdemDeServicoPublicDTO consultarStatusPublico(Long osId, String cpfCnpjCliente) {
        log.info("Consulta pública da OS {} com CPF/CNPJ: {}", osId, cpfCnpjCliente);
        
        OrdemDeServico os = ordemDeServicoRepository.findById(osId)
            .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço", osId));
        
        // Verificar se a OS pertence ao cliente que está consultando
        if (!os.getCliente().getCpfCnpj().equals(cpfCnpjCliente)) {
            log.warn("Tentativa de acesso não autorizado à OS {} com CPF/CNPJ: {}", osId, cpfCnpjCliente);
            throw new ResourceNotFoundException("Ordem de Serviço", osId);
        }
        
        log.info("Consulta pública autorizada para OS {}", osId);
        return OrdemDeServicoPublicDTO.fromEntity(os);
    }

    /**
     * Calcula o tempo médio de execução das Ordens de Serviço finalizadas.
     * Considera apenas OSs com data de início e finalização definidas.
     * 
     * @return DTO com estatísticas de tempo médio de execução
     */
    @Transactional(readOnly = true)
    public MonitoramentoDTO calcularTempoMedioExecucao() {
        log.info("Calculando tempo médio de execução das OSs");
        
        // Buscar todas as OSs finalizadas
        List<OrdemDeServico> osFinalizadas = ordemDeServicoRepository.findByStatus(StatusOrdemServico.FINALIZADA);
        
        // Adicionar também as OSs entregues
        List<OrdemDeServico> osEntregues = ordemDeServicoRepository.findByStatus(StatusOrdemServico.ENTREGUE);
        List<OrdemDeServico> todasFinalizadas = new ArrayList<>();
        todasFinalizadas.addAll(osFinalizadas);
        todasFinalizadas.addAll(osEntregues);
        
        // Filtrar apenas as que têm data de início e finalização
        List<Duration> temposExecucao = todasFinalizadas.stream()
            .filter(os -> os.getDataInicioExecucao() != null && os.getDataFinalizacao() != null)
            .map(os -> Duration.between(os.getDataInicioExecucao(), os.getDataFinalizacao()))
            .toList();
        
        if (temposExecucao.isEmpty()) {
            log.info("Nenhuma OS finalizada com dados de tempo disponíveis");
            return new MonitoramentoDTO(0.0, 0L, 0.0, 0.0);
        }
        
        // Calcular estatísticas
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

