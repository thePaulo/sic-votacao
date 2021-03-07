package mygroup.voting.topicTests;

import mygroup.voting.model.Topic;
import mygroup.voting.service.TopicService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class VotingTest {
    
    @Mock
    private TopicService topicService;
    
    @Test
    public void mockedTopicDefaultCreation(){
        Topic t = new Topic("Nova pauta",3L);
        topicService.addNewTopic(t);

        Mockito.when(topicService.getTopic(3L)).thenReturn(Optional.of(t));
        
        assertThat(topicService.getTopic(3L).get().getDescription()).isEqualTo("Nova pauta");
        
        assertThat(topicService.getTopic(3L).get().getTimeLimit()).isEqualTo(60);

        assertThat(topicService.getTopic(3L).get().getPositive()).isEqualTo(0);

        assertThat(topicService.getTopic(3L).get().getNegative()).isEqualTo(0);
    }
    
    @Test
    public void addRepeatedTopicIdTestError(){
        Topic t = new Topic("Nova pauta",3L);
        topicService.addNewTopic(t);
        
        Topic t2 = new Topic("pauta aleatória",3L);

        doNothing().doThrow(new IllegalStateException()).when(topicService).addNewTopic(t2);
        
        topicService.addNewTopic(t2);
    }
    
    @Test
    public void deleteTopicTest(){
        topicService.addNewTopic( new Topic("Pauta padrão",3L));
        
        topicService.deleteTopic(3L);
        
        verify(topicService,times(1)).deleteTopic(3L);
    }
}
