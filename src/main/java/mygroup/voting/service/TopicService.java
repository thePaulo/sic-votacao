package mygroup.voting.service;

import com.rabbitmq.client.AMQP;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import mygroup.voting.dao.TopicDao;
import mygroup.voting.dao.VoteDao;
import mygroup.voting.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeoutException;


@Service
public class TopicService {
    
    Logger logger = LoggerFactory.getLogger(TopicService.class);

    private final TopicDao topicDao;
    private final VoteDao voteDao;

    @Autowired
    public TopicService(TopicDao topicDao , VoteDao voteDao) {
        this.topicDao = topicDao;
        this.voteDao = voteDao;
    }

    public Optional<Topic> getTopic(Long id){
        return topicDao.findById(id);
    }

    @Cacheable(value="topics")
    public List<Topic> getTopics(){
        return topicDao.findAll();
    }

    /** Adição da pauta a ser persistida, somente caso não haja outra previamente criada
     *  
     * 
     * @param topic pauta a ser adicionada no sistema
     */
    @CacheEvict(value="topics",allEntries = true)
    public void addNewTopic(Topic topic) {
        Optional<Topic> topicOptional = topicDao.findById(topic.getId());
        if ( topicOptional.isPresent()){
            logger.error("Pauta com id repetido");
            throw new IllegalStateException("Pauta com id " +topic.getId()+ " já existe"); //já há uma pauta registrada com esse id
        }
        if(topic.getTimeLimit() == 0){ //caso não seja informado o tempo limite de expiração da pauta, 60s será setado
            topic.setTimeLimit(60);
        }
        topic.setCreation(LocalDateTime.now()); //data atual de criação da pauta
        topicDao.save(topic); //persistindo a pauta
        logger.info("Pauta salva no sistema");
        
        try {
            sendDelayedMessage(topic); //usando serviço de mensageria para alertar quando a pauta for finalizada
            logger.info("Mensagem de expiração de pauta submetida");
        }catch(Exception e){ //erro causado que será ou IOException ou TimeoutException
            System.out.println(e.getCause().getMessage());
            logger.error("Erro no serviço de mensageria temporizada");
        }
        
    }

    /** deleção de uma pauta já registrada no sistema
     * 
     * @param topicId tópico da pauta
     */

    @CacheEvict(value="topics",allEntries = true)
    public void deleteTopic(Long topicId) {
        boolean exists = topicDao.existsById(topicId);
        if (!exists){ //caso esta pauta esteja registrada no sistema
            
            logger.error("Pauta não registrada no sistema");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Pauta com id " + topicId + " não existe"
            );
        }
        voteDao.deleteVotesByTopicId(topicId);
        
        logger.info("Pauta deletada");
        topicDao.deleteById(topicId); //removendo do banco esta pauta
    }

    /** Atualização de uma pauta somente caso esteja dentro do tempo limite de atualização
     * 
     * @param topicId id da pauta
     * @param description descrição dos conteúdos da pauta
     * @param positive quantidade de votos a favor da pauta
     * @param negative quantidade de votos contra a pauta
     */
    @CacheEvict(value="topics",allEntries = true)
    @Transactional
    public void updateTopic(Long topicId, String description,int positive,int negative) {
        Topic topic = topicDao.findById(topicId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Pauta com id "+topicId+" não existe"
        ));//caso tópico não exista
        
        LocalDateTime now = LocalDateTime.now();
        Date d1 = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date d2 = Date.from(topic.getCreation().atZone(ZoneId.systemDefault()).toInstant());

        long dif = (d1.getTime()-d2.getTime())/1000; //diferença entre a hora atual e a hora de criação da pauta

        if( dif <= topic.getTimeLimit()) {// se o tempo entre esta requisição for abaixo do tempo de limite da pauta
            if (description != null && description.length() > 0 && !Objects.equals(topic.getDescription(), description)) {// atualização de descrição caso dados informados sejam válidos
                topic.setDescription(description);
            }if(positive >=0 && negative >=0){ //validação dos votos informados
                topic.setPositive(topic.getPositive()+positive);
                topic.setNegative(topic.getNegative()+negative);

                try {
                    sendNonDelayedMessage(topic); // serviço de mensageria alertando alteração numa pauta
                }catch (Exception e){
                    System.out.println(e.getCause().getMessage());
                    logger.error("Erro no serviço de mensageria não temporizada");
                }
            }
        }else{
            logger.error("Votação nesta pauta já foi encerrada");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Erro - Sessão de votação já terminada");// tempo de votação para esta pauta já foi expirado
        }
    }

    /** Envia mensagem para o serviço de mensageria quando a pauta for encerrada após X segundos do tempo limite
     *  previamente especificado dentro da própria pauta
     * 
     * @param topic pauta recém criada
     * @throws IOException erro causado neste caso normalmente se não for encontrado a fila especificada
     * @throws TimeoutException erro causado caso não esteja tendo conexão com o servidor do rabbitmq dentro de certo tempo
     */
    public void sendDelayedMessage(Topic topic) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        
        try(Connection connection = factory.newConnection()){
            Channel channel = connection.createChannel();
            
            //queue durable exclusive autodelete arguments
            channel.queueDeclare("my_delayed_voting_queue",true,false,false,null);
                        
            
            Map<String,Object> args = new HashMap<String,Object>();
            args.put("x-delayed-type","direct");
            channel.exchangeDeclare("my_delayed_exchange","x-delayed-message",true,false,args);

            channel.queueBind("my_delayed_voting_queue","my_delayed_exchange","my_delayed_routingKey");
            
            Map<String,Object> headers = new HashMap<String,Object>();
            headers.put("x-delay",1000*topic.getTimeLimit());
            AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder().headers(headers);
            
            channel.basicPublish("my_delayed_exchange","my_delayed_routingKey",props.build(),("Votação na pauta\n " +
                    topic.getId()+" com descrição: \n" +
                    topic.getDescription()+" foi encerrada").getBytes(StandardCharsets.UTF_8));

            logger.info("Serviço de mensageria com delay feito com sucesso");
        }
    }
    
    public void sendNonDelayedMessage(Topic topic) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        try(Connection connection = factory.newConnection()){
            Channel channel = connection.createChannel();

            channel.queueDeclare("myvoting_queue",true,false,false,null);

            channel.basicPublish("", "myvoting_queue", null, (" pauta: "+
                    topic.getId()+"\n está com "+topic.getPositive()+ " votos a favor e "+
                    topic.getNegative()+ " votos contra").getBytes(StandardCharsets.UTF_8));

            logger.info("Serviço de mensageria padrão feito com sucesso");
        }
    }
}
