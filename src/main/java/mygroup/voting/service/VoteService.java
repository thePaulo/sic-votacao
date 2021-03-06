package mygroup.voting.service;

import mygroup.voting.dao.VoteDao;
import mygroup.voting.model.Topic;
import mygroup.voting.model.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class VoteService {
    private final VoteDao voteDao;
    private final TopicService topicService;

    @Autowired
    public VoteService(VoteDao voteDao, TopicService topicService) {
        this.voteDao = voteDao;
        this.topicService = topicService;
    }

    @Cacheable("fetchVotos")
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"This topic does not exist");
        }
        fetchData(vote.getAssociateId()); //checagem em api externa dos dados sobre o cpf deste voto


        Optional<Vote> voteOptional = voteDao.findById(vote.getId());
        if ( voteOptional.isPresent()){
            throw new IllegalStateException("vote id already taken");
        }

        voteOptional = voteDao.findVoteByAssociateId(vote.getAssociateId());
        if ( voteOptional.isPresent()){
            throw new IllegalStateException("associate has already voted");
        }

        voteDao.save(vote); //persistindo este voto

        //atualizando os dados de pauta dado o voto a favor ou contra
        if (vote.isValue()){
            topicService.updateTopic(vote.getTopicId(),"",1,0);
        }else{
            topicService.updateTopic(vote.getTopicId(),"",0,1);
        }

    }

    //19839091069
    //62289608068

    /** Busca/validação em API externa dos cpfs válidos
     * 
     * @param associateId cpf do associado que está votando
     */
    @Cacheable("fetchCPF")
    void fetchData(Long associateId){
        String response;
        try {
            response = new RestTemplate().getForObject("https://user-info.herokuapp.com/users/"+associateId, String.class);
        }catch (Exception e){   //404 error
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Associate CPF wasn't found"); //caso este cpf não seja registrado na api = não é válido
        }
        if(response.equals("{\"status\":\"ABLE_TO_VOTE\"}"));
        else if(response.equals("{\"status\":\"UNABLE_TO_VOTE\"}")){ //caso este cpf está registrado na api porém não está permitido seu voto
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"associate with id: "+ associateId +" isn't allowed to vote");
        }
    }

}
