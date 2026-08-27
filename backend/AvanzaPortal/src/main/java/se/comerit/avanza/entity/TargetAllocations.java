package se.comerit.avanza.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "target_allocations")
public class TargetAllocations {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String account_type;
    private Double target_pct;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public TargetAllocations() {
    }

    public TargetAllocations(String account_type, Double target_pct, User user) {
        this.account_type = account_type;
        this.target_pct = target_pct;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public Double getTarget_pct() {
        return target_pct;
    }

    public void setTarget_pct(Double target_pct) {
        this.target_pct = target_pct;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
