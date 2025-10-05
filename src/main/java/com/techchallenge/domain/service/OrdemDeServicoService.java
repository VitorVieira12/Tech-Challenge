package com.techchallenge.domain.service;

import com.techchallenge.domain.dto.OrdemDeServicoInputDTO;
import com.techchallenge.domain.dto.OrdemDeServicoResponseDTO;
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
import java.util.ArrayList;
import java.util.List;

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
}

