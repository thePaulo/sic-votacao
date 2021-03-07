package mygroup.voting.dao;


import mygroup.voting.model.Topic;
import mygroup.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 *  Interface entre o banco de dados e a camada de serviços da pauta
 */
@Repository
public interface TopicDao extends JpaRepository<Topic, Long> { 

    @Query("SELECT t FROM Topic t WHERE t.description = ?1")
    @QueryHints(@QueryHint(name=org.hibernate.annotations.QueryHints.CACHEABLE,value="true"))
    Optional<Topic> findTopicByDescription(String description);

    @Query(value="SELECT * FROM Topic t",nativeQuery = true)
    @QueryHints(@QueryHint(name=org.hibernate.annotations.QueryHints.CACHEABLE,value="true"))
    List<Topic> findAll();
}
