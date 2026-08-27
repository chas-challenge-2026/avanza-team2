package se.comerit.avanza.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import java.util.List;

/**
 * JPA Entity representing a user account in the Avanza portfolio system.
 * Maps to the 'users' table in PostgreSQL.
 * 
 * Relationships:
 * <p/>
 * - One User has many Accounts
 * <p/>
 * - One User has many TargetAllocations
 * <p/>
 * - One User has many Alerts
 * <p/>
 * 
 * Note: Password field will be migrated from MD5 to BCrypt.
 */
@Entity
@Table(name = "users")
public class User {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    // TODO: Password hashing should be migrated to bcrypt along with the
    private String password_md5;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TargetAllocations> targetAllocations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alerts> alerts;

    // Constructors
    public User() {
    }

    public User(String name, String email, String password_md5) {
        this.name = name;
        this.email = email;
        this.password_md5 = password_md5;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword_md5() {
        return password_md5;
    }

    public void setPassword_md5(String password_md5) {
        this.password_md5 = password_md5;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<TargetAllocations> getTargetAllocations() {
        return targetAllocations;
    }

    public void setTargetAllocations(List<TargetAllocations> targetAllocations) {
        this.targetAllocations = targetAllocations;
    }

    public List<Alerts> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alerts> alerts) {
        this.alerts = alerts;
    }
}
