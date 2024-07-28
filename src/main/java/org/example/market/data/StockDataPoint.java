package org.example.market.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockDataPoint {
    private LocalDateTime dateTime;
    private BigDecimal price;
    private String symbol;

    public StockDataPoint(LocalDateTime dateTime, BigDecimal price, String symbol) {
        this.dateTime = dateTime;
        this.price = price;
        this.symbol = symbol;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSymbol() {
        return symbol;
    }
}
