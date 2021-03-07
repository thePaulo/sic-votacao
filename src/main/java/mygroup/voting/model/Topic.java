package mygroup.voting.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;

/** modelo da Pauta do sistema de votação
 * 
 */

@Entity
@Table(indexes = @Index(columnList = "description"))
@org.hibernate.annotations.Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class Topic {
    @Id
    private Long id;
    private String description; //descrição
    private LocalDateTime creation; //data de criação
    private int timeLimit; //tempo limite em segundos
    private int positive; //votos a favor da pauta
    private int negative; //votos contra a pauta

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Topic() {
    }

    public Topic(String description) {
        this.description = description;
    }

    public Topic(String description, int timeLimit) {
        this.description = description;
        this.timeLimit = timeLimit;
    }

    public Topic(String description, Long id){
        this.description = description;
        this.id = id;
    }

    public Topic(String description, int timeLimit, Long id) {
        this.description = description;
        this.timeLimit = timeLimit;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public int getPositive() {
        return positive;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public int getNegative() {
        return negative;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }
}
