package mygroup.voting.dao;


import mygroup.voting.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *  Interface entre o banco de dados e a camada de serviços da pauta
 */
@Repository
public interface TopicDao extends JpaRepository<Topic, Long> { 

    @Query("SELECT t FROM Topic t WHERE t.description = ?1")
    Optional<Topic> findTopicByDescription(String description);
}
