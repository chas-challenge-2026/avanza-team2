package se.comerit.avanza.entity;

import java.sql.Timestamp;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity representing an alert for a user in the Avanza portfolio system.
 * <p/>
 * Maps to the 'alerts' table in PostgreSQL.
 * 
 * Relationships:
 * <p/>
 * - Many Alerts belong to one User
 */
@Entity
@Table(name = "alerts")
public class Alerts {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alert_type;
    private String message;
    private Boolean dismissed;
    private Timestamp created_at;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public Alerts() {
    }

    public Alerts(String alert_type, String message, Boolean dismissed, Timestamp created_at, User user) {
        this.alert_type = alert_type;
        this.message = message;
        this.dismissed = dismissed;
        this.created_at = created_at;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getDismissed() {
        return dismissed;
    }

    public void setDismissed(Boolean dismissed) {
        this.dismissed = dismissed;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
