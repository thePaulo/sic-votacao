package mygroup.voting.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 *  modelo do Voto do sistema de votação
 */

@Entity
@Table
@org.hibernate.annotations.Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class Vote {
    @Id
    private Long id;
    private Long topicId; //id da pauta a ser votada
    private Long associateId; //cpf do associado que votou
    private boolean value; // valor do voto: 0 - contra, 1 - a favor 

    public Vote(){

    }

    public Vote(Long topicId, Long associateId, boolean value) {
        this.topicId = topicId;
        this.associateId = associateId;
        this.value = value;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getAssociateId() {
        return associateId;
    }

    public void setAssociateId(Long associateId) {
        this.associateId = associateId;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
