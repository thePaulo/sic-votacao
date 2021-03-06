package mygroup.voting.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe serve para configuração do serviço de mensageria que será utilizado com o RabbitMQ
 * 
 * @author paulov 
 */
@Configuration
public class MessagingConfig {

    public static final String QUEUE = "myvoting_queue";//fila que receberá as mensagens enviadas no servidor do rabbitmq 
    public static final String EXCHANGE = "my_exchange";//responsável pelo roteamento das mensagens
    public static final String ROUTING_KEY = "my_routingKey";//vínculo de conexão entre a fila de msgs e a exchange responsável do roteamento

    //getter
    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

    //getter
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    /**
     * retorna o binding da fila com o 'roteador de mensageria' pela chave de roteamento
     * 
     * @param queue fila de mensagens no sistema 
     * @param exchange responsável pelo roteamento da mensagem
     * @return Classe que conecta a queue e a exchange pela routing key
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * 
     * @return conversor de objetos para o formato json
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 
     * @param connectionFactory criador de instâncias de conexão com o rabbitmq
     * @return template de uma instância do protocolo de mensageria avançada
     */
    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}