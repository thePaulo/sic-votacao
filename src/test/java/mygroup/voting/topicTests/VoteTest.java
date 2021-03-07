package mygroup.voting.topicTests;

import mygroup.voting.model.Topic;
import mygroup.voting.model.Vote;
import mygroup.voting.service.TopicService;
import mygroup.voting.service.VoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
public class VoteTest {
    
    @Mock
    VoteService voteService;
    
    @Mock
    TopicService topicService;
    
    @Test
    public void testSuccessfullVotes(){
        Topic topic = new Topic("pauta inicial",3L);
        
        topicService.addNewTopic(topic);
        voteService.addNewVote(new Vote(3L,19839091069L,true));
        voteService.addNewVote(new Vote(3L,62289608068L,true));

        Optional<Topic> ot = Optional.of(topic);
        ot.get().setPositive(2);
        
        when(topicService.getTopic(3L)).thenReturn(ot);
        Assertions.assertEquals(topicService.getTopic(3L).get().getPositive(),2);
    }
    
    @Test
    public void testVoteOnNonExistingVoting(){
        Vote vote = new Vote(3L,19839091069L,false);
        
        doNothing().doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,"Esta pauta não existe"))
                .when(voteService).addNewVote(vote);

        voteService.addNewVote(vote);
    }
    
    @Test
    public void testNotValidCPF(){
        topicService.addNewTopic(new Topic("pauta inicial",3L));
        
        doNothing().doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"CPF do associado não encontrado"))
                .when(voteService).addNewVote(new Vote(3L,999999999999L,true));
    }

    @Test
    public void testDoubleVotingSameId(){
        topicService.addNewTopic(new Topic("pauta inicial",3L));
        
        Vote fvote = new Vote(3L,19839091069L,true);
        Vote nvote = new Vote(3L,62289608068L,true);
        
        fvote.setId(0L);
        nvote.setId(0L);
        
        voteService.addNewVote( fvote);

        doNothing().doThrow(new IllegalStateException("Este Id já foi cadastrado"))
                .when(voteService).addNewVote(nvote);
    }
    
    @Test
    public void testDoubleVoteSameAssociate(){
        topicService.addNewTopic(new Topic("pauta inicial",3L));

        voteService.addNewVote( new Vote(3L,19839091069L,true) );
        
        doNothing().doThrow(new IllegalStateException("Associado já votou nesta pauta"))
            .when(voteService).addNewVote(new Vote(3L,19839091069L,false));
    }
}
