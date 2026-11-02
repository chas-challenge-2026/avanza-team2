package se.comerit.avanza.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "position_snapshot")
public class PositionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal closePrice;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "sek_value", nullable = false, precision = 18, scale = 6)
    private BigDecimal sekValue;

    @Column(name = "currency_fx", nullable = false, precision = 18, scale = 8)
    private BigDecimal currencyFx;

    @Column(name = "target_allocation", precision = 5, scale = 2)
    private BigDecimal targetAllocation;

    protected PositionSnapshot() {
    }

    public PositionSnapshot(
            Long userId,
            Long accountId,
            String ticker,
            String currency,
            BigDecimal closePrice,
            BigDecimal quantity,
            LocalDate priceDate,
            BigDecimal sekValue,
            BigDecimal currencyFx,
            BigDecimal targetAllocation) {
        this.userId = userId;
        this.accountId = accountId;
        this.ticker = ticker;
        this.currency = currency;
        this.closePrice = closePrice;
        this.quantity = quantity;
        this.priceDate = priceDate;
        this.sekValue = sekValue;
        this.currencyFx = currencyFx;
        this.targetAllocation = targetAllocation;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getTicker() {
        return ticker;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public BigDecimal getSekValue() {
        return sekValue;
    }

    public BigDecimal getCurrencyFx() {
        return currencyFx;
    }

    public BigDecimal getTargetAllocation() {
        return targetAllocation;
    }
}
