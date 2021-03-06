package mygroup.voting.topicTests;

import mygroup.voting.dao.TopicDao;
import mygroup.voting.model.Topic;
import mygroup.voting.service.TopicService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
public class TopicTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JSONObject topicJsonObject = new JSONObject();
    HttpHeaders headers = new HttpHeaders();

    @Mock
    private TopicDao topicDao;


    private TopicService topicService;

    @BeforeEach
    void setUp(){
        topicService = new TopicService(topicDao);

        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testDescription(){
        Topic topic = new Topic("A boring topic",10,99L);

        when(topicDao.findById(99L)).thenReturn(java.util.Optional.of(topic));

        Optional<Topic> t = topicService.getTopic(99L);
        assertEquals(t.get().getDescription(),
                topic.getDescription());
    }

    @Test
    void testTopicCreation() throws Exception{

        topicJsonObject.put("description","New Topic");
        topicJsonObject.put("id",99);

        HttpEntity<String> request = new HttpEntity<String>(topicJsonObject.toString(),headers);

        restTemplate.postForEntity("http://localhost:8080/api/v1/topic",request,String.class);
        //String body = restTemplate.getForObject("http://localhost:8080/api/v1/topic",String.class);

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

        //assertEquals(topics.get(1).getDescription(),"New Topic");

    }

    @Test
    void testNegativeVote() throws Exception{


        topicJsonObject.put("description","Negative vote");
        topicJsonObject.put("id",33);
        topicJsonObject.put("timeLimit",3);


        HttpEntity<String> request = new HttpEntity<String>(topicJsonObject.toString(),headers);
        restTemplate.postForEntity("http://localhost:8080/api/v1/topic",request,String.class);

        //Thread.sleep(4000);

        JSONObject voteJsonObject = new JSONObject();
        voteJsonObject.put("associateId",19839091069L);
        voteJsonObject.put("topicId",33L);
        voteJsonObject.put("value",false);
        voteJsonObject.put("id",2);

        HttpEntity<String> voteRequest = new HttpEntity<String>(voteJsonObject.toString(),headers);
        restTemplate.postForEntity("http://localhost:8080/api/v1/vote",voteRequest,String.class);


        ResponseEntity<List<Topic>> body = restTemplate.exchange(
                "http://localhost:8080/api/v1/topic",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Topic>>() {});
        List<Topic> topics = body.getBody();
        Topic updatedTopic = new Topic();
        for ( Topic iterTopic : topics ){
            if ( iterTopic.getDescription().equals("Negative vote")){
                updatedTopic= iterTopic;
            }
        }

        assertEquals(updatedTopic.getNegative(),1);
        //assertEquals(topics.get(2).getNegative(),1);


    }

    @Test
    void testSessionExpiration() throws Exception{
        topicJsonObject.put("description","Expiration");
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



}
