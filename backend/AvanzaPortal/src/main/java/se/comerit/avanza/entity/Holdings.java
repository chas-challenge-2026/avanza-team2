package se.comerit.avanza.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity representing a holding for an account in the Avanza portfolio
 * system.
 * <p/>
 * Maps to the 'holdings' table in PostgreSQL.
 * 
 * Relationships:
 * <p/>
 * - Many Holdings belong to one Account
 */
@Entity
@Table(name = "holdings")
public class Holdings {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private String instrument_name;
    private BigDecimal quantity;
    private BigDecimal avg_buy_price;
    private String currency;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Constructors
    public Holdings() {
    }

    public Holdings(String ticker, String instrument_name, BigDecimal quantity, BigDecimal avg_buy_price, String currency,
            Account account) {
        this.ticker = ticker;
        this.instrument_name = instrument_name;
        this.quantity = quantity;
        this.avg_buy_price = avg_buy_price;
        this.currency = currency;
        this.account = account;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInstrument_name() {
        return instrument_name;
    }

    public void setInstrument_name(String instrument_name) {
        this.instrument_name = instrument_name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvg_buy_price() {
        return avg_buy_price;
    }

    public void setAvg_buy_price(BigDecimal avg_buy_price) {
        this.avg_buy_price = avg_buy_price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAvgBuy() {
        return avg_buy_price;
    }
}
