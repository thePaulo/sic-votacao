package mygroup.voting.dao;


import mygroup.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 *  Interface entre o banco de dados e a camada de serviços do voto
 */
@Repository
public interface VoteDao extends JpaRepository<Vote, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Vote v WHERE v.topicId = ?1")
    @QueryHints(@QueryHint(name=org.hibernate.annotations.QueryHints.CACHEABLE,value="true"))
    void deleteVotesByTopicId(Long topicId);

    @Query("SELECT v FROM Vote v WHERE v.associateId = ?1 AND v.topicId = ?2")
    @QueryHints(@QueryHint(name=org.hibernate.annotations.QueryHints.CACHEABLE,value="true"))
    Optional<Vote> findVoteByAssociateId(Long associateId,Long topicId);
    
    
    @Query(value="SELECT * FROM Vote v",nativeQuery = true)
    @QueryHints(@QueryHint(name=org.hibernate.annotations.QueryHints.CACHEABLE,value="true"))
    List<Vote> findAll();
    
}
