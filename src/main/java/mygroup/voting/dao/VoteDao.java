package mygroup.voting.dao;


import mygroup.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *  Interface entre o banco de dados e a camada de serviços do voto
 */
@Repository
public interface VoteDao extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v WHERE v.associateId = ?1")
    Optional<Vote> findVoteByAssociateId(Long associateId);
}
