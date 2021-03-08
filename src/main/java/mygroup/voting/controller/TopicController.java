package mygroup.voting.controller;

import mygroup.voting.model.Topic;
import mygroup.voting.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/topic")
public class TopicController {
    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /** endpoint da api de pauta que cuida das requisições GET e leva para a camada de serviços
     * 
     * @return lista de todas as pautas do sistema
     */
    @GetMapping
    public List<Topic> getTopics(){
        return topicService.getTopics();
    }

    /** endpoint da api de pauta que cuida das requisições POST e leva para a camada de serviços
     * 
      * @param topic pauta a ser adicionada informado pelo corpo da requisição
     */ 
    @PostMapping
    public void registerNewTopic(@RequestBody Topic topic){
        topicService.addNewTopic(topic);
    }

    /** endpoint da api de pauta que cuida das requisições DELETE e leva para a camada de serviços
     *
     * @param topicId id da pauta a ser deletada
     */
    @DeleteMapping(path="{Id}")
    public void deleteTopic(@PathVariable("Id") Long topicId){
        topicService.deleteTopic(topicId);
    }

    /** endpoint da api de pauta que cuida das requisições PUT e leva para a camada de serviços
     *
     * @param topicId   id da pauta a ser modificada
     * @param description   (opcional) descrição a ser alterada 
     * @param positive  (opcional) voto a favor de uma pauta
     * @param negative (opcional) voto contra uma pauta
     */
    @PutMapping(path="{Id}")
    public void updateTopic(@PathVariable("Id") Long topicId,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) int positive,
                            @RequestParam(required = false) int negative){
        topicService.updateTopic(topicId,description,positive,negative);
    }
}
