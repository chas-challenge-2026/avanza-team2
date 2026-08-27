package se.comerit.avanza.entity;

<<<<<<< HEAD
import jakarta.persistence.FetchType;
=======
>>>>>>> 678c97e00e138db96ef69607aca01d6dfe8e02e8
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

<<<<<<< HEAD
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
=======
>>>>>>> 678c97e00e138db96ef69607aca01d6dfe8e02e8
@Entity
@Table(name = "holdings")
public class Holdings {
    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private String instrument_name;
    private Integer quantity;
    private Double avg_buy_price;
    private String currency;

    // Relationships
<<<<<<< HEAD
    @ManyToOne(fetch = FetchType.LAZY)
=======
    @ManyToOne
>>>>>>> 678c97e00e138db96ef69607aca01d6dfe8e02e8
    @JoinColumn(name = "account_id")
    private Account account;

    // Constructors
    public Holdings() {
    }

    public Holdings(String ticker, String instrument_name, Integer quantity, Double avg_buy_price, String currency,
            Account account) {
        this.ticker = ticker;
        this.instrument_name = instrument_name;
        this.quantity = quantity;
        this.avg_buy_price = avg_buy_price;
        this.currency = currency;
        this.account = account;
    }

    // Getters and Setters
<<<<<<< HEAD
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

=======
>>>>>>> 678c97e00e138db96ef69607aca01d6dfe8e02e8
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getAvg_buy_price() {
        return avg_buy_price;
    }

    public void setAvg_buy_price(Double avg_buy_price) {
        this.avg_buy_price = avg_buy_price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

<<<<<<< HEAD
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
=======
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
>>>>>>> 678c97e00e138db96ef69607aca01d6dfe8e02e8
