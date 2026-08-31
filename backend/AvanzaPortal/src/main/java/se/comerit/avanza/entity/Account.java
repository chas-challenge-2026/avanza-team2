package se.comerit.avanza.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import jakarta.persistence.JoinColumn;

/**
 * JPA Entity representing an account for a user in the Avanza portfolio system.
 * <p/>
 * Maps to the 'accounts' table in PostgreSQL.
 * 
 * Relationships:
 * <p/>
 * - Many Accounts belong to one User
 * - One Account can have many Holdings
 */
@Entity
@Table(name = "accounts")
public class Account {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String account_type;
    private String account_name;
    private String currency;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holdings> holdings;

    // Constructors
    public Account() {
    }

    public Account(String account_type, String account_name, String currency, User user,
            List<Holdings> holdings) {
        this.account_type = account_type;
        this.account_name = account_name;
        this.currency = currency;
        this.user = user;
        this.holdings = holdings;
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

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holdings> holdings) {
        this.holdings = holdings;
    }
}
