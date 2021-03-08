package mygroup.voting.service;

import mygroup.voting.dao.VoteDao;
import mygroup.voting.model.Topic;
import mygroup.voting.model.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class VoteService {

    Logger logger = LoggerFactory.getLogger(TopicService.class);
    private final VoteDao voteDao;
    private final TopicService topicService;

    @Autowired
    public VoteService(VoteDao voteDao, TopicService topicService) {
        this.voteDao = voteDao;
        this.topicService = topicService;
    }

    
    public List<Vote> getVotes(){
        return voteDao.findAll();
    }

    /** Adicionando um voto em alguma pauta do sistema
     * 
     * @param vote voto de uma pauta a ser validado e caso positivo, persistido no sistema
     */
    
    @Transactional
    public void addNewVote(Vote vote) {
        Optional<Topic> topicOptional = topicService.getTopic(vote.getTopicId());
        if ( !topicOptional.isPresent()){ //caso a pauta não exista no sistema
            logger.error("Pauta não existente");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Esta pauta não existe");
        }
        fetchData(vote.getAssociateId()); //checagem em api externa dos dados sobre o cpf deste voto


        Optional<Vote> voteOptional = voteDao.findById(vote.getId());
        if ( voteOptional.isPresent()){
            logger.error("Este voto já foi cadastrado");
            throw new IllegalStateException("Voto com este Id já foi registrado");
        }

        voteOptional = voteDao.findVoteByAssociateId(vote.getAssociateId(),vote.getTopicId());
        if ( voteOptional.isPresent()){
            logger.error("Associado já votou nesta pauta");
            throw new IllegalStateException("Associado com voto já cadastrado para esta pauta");
        }

        voteDao.save(vote); //persistindo este voto

        //atualizando os dados de pauta dado o voto a favor ou contra
        if (vote.isValue()){
            topicService.updateTopic(vote.getTopicId(),"",1,0);
        }else{
            topicService.updateTopic(vote.getTopicId(),"",0,1);
        }
        logger.info("Voto contabilizado na pauta");

    }

    //19839091069
    //62289608068

    /** Busca/validação em API externa dos cpfs válidos
     * 
     * @param associateId cpf do associado que está votando
     */
    void fetchData(Long associateId){
        String response="";
        try {
            response = new RestTemplate().getForObject("https://user-info.herokuapp.com/users/"+associateId, String.class);
        }catch (HttpClientErrorException e){   
            if( e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){ //Erro no serviço de API
                logger.error("Não foi possível se comunicar com a API externa");    
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Não foi possível se comunicar com a API");
            }
            else{
                logger.error("CPF não encontrado");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"CPF inválido"); //caso este cpf não seja registrado na api = não é válido    
            }
        }
        if(response.equals("{\"status\":\"ABLE_TO_VOTE\"}"));
        else if(response.equals("{\"status\":\"UNABLE_TO_VOTE\"}")){ //caso este cpf está registrado na api porém não está permitido seu voto
            logger.error("Associado não está permitido a votar");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Associado com id: "+ associateId +" não foi permitido o voto");
        }
    }

}
