package org.example.market.model;

import java.math.BigDecimal;
import java.util.List;

public abstract class FinancialInstrument {
    private final String symbol;
    private final String name;
    private BigDecimal currentPrice;
    private List<BigDecimal> historicalPrices;

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

    public void setHistoricalPrices(List<BigDecimal> historicalPrices) {
        this.historicalPrices = historicalPrices;
    }
}
