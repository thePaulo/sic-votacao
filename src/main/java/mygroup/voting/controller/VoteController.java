package mygroup.voting.controller;

import mygroup.voting.model.Vote;
import mygroup.voting.service.VoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "api/v1/vote")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /** endpoint de votos que lida com requisições GET 
     * 
     * @return lista de votos registrados
     */
    @GetMapping
    public List<Vote> getVotes(){
        return voteService.getVotes();
    }

    /** endpoint de votos que lida com requisições POST
     * 
     * @param vote voto a ser tratado na camada de serviços
     */

    @PostMapping
    public void registerNewVote(@RequestBody Vote vote){
        voteService.addNewVote(vote);
    }

}
