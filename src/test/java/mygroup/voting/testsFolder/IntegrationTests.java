package mygroup.voting.testsFolder;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IntegrationTests {
    /*
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

    */
}
