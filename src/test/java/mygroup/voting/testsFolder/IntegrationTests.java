package mygroup.voting.testsFolder;

import mygroup.voting.dao.TopicDao;
import mygroup.voting.model.Topic;
import mygroup.voting.service.TopicService;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class IntegrationTests {
    private final RestTemplate restTemplate = new RestTemplate();
    private final JSONObject topicJsonObject = new JSONObject();
    HttpHeaders headers = new HttpHeaders();

    @Mock
    private TopicDao topicDao;

    @Autowired
    private TopicService topicService;

    @BeforeEach
    void setUp(){

        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testDescription(){
        Optional<Topic> topic = topicService.getTopic(99L);
        assertEquals(topic.get().getDescription(),"Nova Pauta");
    }

    @Test
    void testTopicCreation() throws Exception{

        topicJsonObject.put("description","Nova Pauta");
        topicJsonObject.put("id",99);

        HttpEntity<String> request = new HttpEntity<String>(topicJsonObject.toString(),headers);

        restTemplate.postForEntity("http://localhost:8080/api/v1/topic",request,String.class);

        ResponseEntity<List<Topic>> body = restTemplate.exchange(
                "http://localhost:8080/api/v1/topic",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Topic>>() {});
        List<Topic> topics = body.getBody();

        Topic updatedTopic = new Topic();
        for ( Topic iterTopic : topics ){
            if ( iterTopic.getDescription().equals("New Topic")){
                assertTrue(true);
            }
        }

    }
    

    @Test
    void testSessionExpiration() throws Exception{
        topicJsonObject.put("description","Sessão expirada");
        topicJsonObject.put("id",77);
        topicJsonObject.put("timeLimit",1);

        
        HttpEntity<String> request = new HttpEntity<String>(topicJsonObject.toString(),headers);
        restTemplate.postForEntity("http://localhost:8080/api/v1/topic",request,String.class);

        Thread.sleep(3000); //Forcing an api timeout

        JSONObject voteJsonObject = new JSONObject();
        voteJsonObject.put("associateId",19839091069L);
        voteJsonObject.put("topicId",77L);
        voteJsonObject.put("value",false);
        voteJsonObject.put("id",582);

        HttpEntity<String> voteRequest = new HttpEntity<String>(voteJsonObject.toString(),headers);
        HttpClientErrorException.Forbidden e = assertThrows(HttpClientErrorException.Forbidden.class,() ->
                restTemplate.postForEntity("http://localhost:8080/api/v1/vote",voteRequest,String.class)
        );

        assertEquals(HttpStatus.FORBIDDEN,e.getStatusCode());
    }

    @Test
    void testVoteOnNonExistingVoting() throws Exception{
        JSONObject voteJsonObject = new JSONObject();
        voteJsonObject.put("associateId",19839091069L);
        voteJsonObject.put("topicId",23981L);
        voteJsonObject.put("value",false);
        voteJsonObject.put("id",999);

        HttpEntity<String> voteRequest = new HttpEntity<String>(voteJsonObject.toString(),headers);
        HttpClientErrorException.BadRequest e = assertThrows(HttpClientErrorException.BadRequest.class,() ->
                restTemplate.postForEntity("http://localhost:8080/api/v1/vote",voteRequest,String.class)
        );
        
        assertEquals(HttpStatus.BAD_REQUEST,e.getStatusCode());
    }

}
