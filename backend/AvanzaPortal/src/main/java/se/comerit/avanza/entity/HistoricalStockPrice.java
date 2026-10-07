package se.comerit.avanza.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the historical stock price for a specific ticker and date.
 * Contains the ticker symbol, the date of the price, and the closing price.
 * HistoricalStockPrice
 */
@Entity
@Table(name = "historical_stock_price")
public class HistoricalStockPrice {

    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal closePrice;

    // Default constructor required by JPA
    protected HistoricalStockPrice() {
    }

    // Constructor
    public HistoricalStockPrice(String ticker, LocalDate priceDate, BigDecimal closePrice) {
        this.ticker = ticker;
        this.priceDate = priceDate;
        this.closePrice = closePrice;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }
}
