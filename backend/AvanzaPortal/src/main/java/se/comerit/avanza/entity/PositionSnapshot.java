package se.comerit.avanza.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Table(name = "position_snapshot")
public class PositionSnapshot {
    @Id
    private Long id;
    private Long userId;
    private Long accountId;
    private String ticker;
    private String currency;
    private Double closePrice;
    private Double quantity;
    private String priceDate;
    private Double sekValue;
    private Double currencyFx;

    public PositionSnapshot() {
    }

    public PositionSnapshot(Long id, Long userId, Long accountId, String ticker, String currency, Double closePrice,
            Double quantity, String priceDate, Double sekValue, Double currencyFx) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.ticker = ticker;
        this.currency = currency;
        this.closePrice = closePrice;
        this.quantity = quantity;
        this.priceDate = priceDate;
        this.sekValue = sekValue;
        this.currencyFx = currencyFx;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(String priceDate) {
        this.priceDate = priceDate;
    }

    public Double getSekValue() {
        return sekValue;
    }

    public void setSekValue(Double sekValue) {
        this.sekValue = sekValue;
    }

    public Double getCurrencyFx() {
        return currencyFx;
    }

    public void setCurrencyFx(Double currencyFx) {
        this.currencyFx = currencyFx;
    }

}
