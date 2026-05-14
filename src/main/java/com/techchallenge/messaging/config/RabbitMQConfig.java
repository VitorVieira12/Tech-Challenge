package com.techchallenge.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Exchange names ────────────────────────────────────────────────────────
    public static final String EXCHANGE_OS       = "os.events";
    public static final String EXCHANGE_BILLING  = "billing.events";
    public static final String EXCHANGE_EXEC     = "execution.events";

    // ─── Queue names (consumed by OS Service) ──────────────────────────────────
    public static final String QUEUE_ORCAMENTO_GERADO    = "os.orcamento.gerado";
    public static final String QUEUE_ORCAMENTO_APROVADO  = "os.orcamento.aprovado";
    public static final String QUEUE_ORCAMENTO_REJEITADO = "os.orcamento.rejeitado";
    public static final String QUEUE_PAGAMENTO_CONFIRMADO = "os.pagamento.confirmado";
    public static final String QUEUE_PAGAMENTO_FALHOU    = "os.pagamento.falhou";
    public static final String QUEUE_EXECUCAO_INICIADA   = "os.execucao.iniciada";
    public static final String QUEUE_EXECUCAO_FINALIZADA = "os.execucao.finalizada";
    public static final String QUEUE_EXECUCAO_FALHOU     = "os.execucao.falhou";

    // ─── Routing keys ──────────────────────────────────────────────────────────
    public static final String RK_OS_CRIADA              = "os.criada";
    public static final String RK_ORCAMENTO_GERADO       = "orcamento.gerado";
    public static final String RK_ORCAMENTO_APROVADO     = "orcamento.aprovado";
    public static final String RK_ORCAMENTO_REJEITADO    = "orcamento.rejeitado";
    public static final String RK_PAGAMENTO_CONFIRMADO   = "pagamento.confirmado";
    public static final String RK_PAGAMENTO_FALHOU       = "pagamento.falhou";
    public static final String RK_EXECUCAO_INICIADA      = "execucao.iniciada";
    public static final String RK_EXECUCAO_FINALIZADA    = "execucao.finalizada";
    public static final String RK_EXECUCAO_FALHOU        = "execucao.falhou";

    // ─── Exchanges ─────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange osExchange() {
        return new TopicExchange(EXCHANGE_OS, true, false);
    }

    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange(EXCHANGE_BILLING, true, false);
    }

    @Bean
    public TopicExchange execExchange() {
        return new TopicExchange(EXCHANGE_EXEC, true, false);
    }

    // ─── Queues consumed by OS Service ─────────────────────────────────────────
    @Bean public Queue queueOrcamentoGerado()    { return QueueBuilder.durable(QUEUE_ORCAMENTO_GERADO).build(); }
    @Bean public Queue queueOrcamentoAprovado()  { return QueueBuilder.durable(QUEUE_ORCAMENTO_APROVADO).build(); }
    @Bean public Queue queueOrcamentoRejeitado() { return QueueBuilder.durable(QUEUE_ORCAMENTO_REJEITADO).build(); }
    @Bean public Queue queuePagamentoConfirmado(){ return QueueBuilder.durable(QUEUE_PAGAMENTO_CONFIRMADO).build(); }
    @Bean public Queue queuePagamentoFalhou()    { return QueueBuilder.durable(QUEUE_PAGAMENTO_FALHOU).build(); }
    @Bean public Queue queueExecucaoIniciada()   { return QueueBuilder.durable(QUEUE_EXECUCAO_INICIADA).build(); }
    @Bean public Queue queueExecucaoFinalizada() { return QueueBuilder.durable(QUEUE_EXECUCAO_FINALIZADA).build(); }
    @Bean public Queue queueExecucaoFalhou()     { return QueueBuilder.durable(QUEUE_EXECUCAO_FALHOU).build(); }

    // ─── Bindings: billing.events → OS queues ──────────────────────────────────
    @Bean
    public Binding bindingOrcamentoGerado(Queue queueOrcamentoGerado, TopicExchange billingExchange) {
        return BindingBuilder.bind(queueOrcamentoGerado).to(billingExchange).with(RK_ORCAMENTO_GERADO);
    }

    @Bean
    public Binding bindingOrcamentoAprovado(Queue queueOrcamentoAprovado, TopicExchange billingExchange) {
        return BindingBuilder.bind(queueOrcamentoAprovado).to(billingExchange).with(RK_ORCAMENTO_APROVADO);
    }

    @Bean
    public Binding bindingOrcamentoRejeitado(Queue queueOrcamentoRejeitado, TopicExchange billingExchange) {
        return BindingBuilder.bind(queueOrcamentoRejeitado).to(billingExchange).with(RK_ORCAMENTO_REJEITADO);
    }

    @Bean
    public Binding bindingPagamentoConfirmado(Queue queuePagamentoConfirmado, TopicExchange billingExchange) {
        return BindingBuilder.bind(queuePagamentoConfirmado).to(billingExchange).with(RK_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public Binding bindingPagamentoFalhou(Queue queuePagamentoFalhou, TopicExchange billingExchange) {
        return BindingBuilder.bind(queuePagamentoFalhou).to(billingExchange).with(RK_PAGAMENTO_FALHOU);
    }

    // ─── Bindings: execution.events → OS queues ────────────────────────────────
    @Bean
    public Binding bindingExecucaoIniciada(Queue queueExecucaoIniciada, TopicExchange execExchange) {
        return BindingBuilder.bind(queueExecucaoIniciada).to(execExchange).with(RK_EXECUCAO_INICIADA);
    }

    @Bean
    public Binding bindingExecucaoFinalizada(Queue queueExecucaoFinalizada, TopicExchange execExchange) {
        return BindingBuilder.bind(queueExecucaoFinalizada).to(execExchange).with(RK_EXECUCAO_FINALIZADA);
    }

    @Bean
    public Binding bindingExecucaoFalhou(Queue queueExecucaoFalhou, TopicExchange execExchange) {
        return BindingBuilder.bind(queueExecucaoFalhou).to(execExchange).with(RK_EXECUCAO_FALHOU);
    }

    // ─── JSON converter and template ───────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
