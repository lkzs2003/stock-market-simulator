package org.example.market.model;

import java.math.BigDecimal;

public abstract class FinancialInstrument {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;

    public FinancialInstrument(String symbol, String name, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void updatePrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
    }
}

