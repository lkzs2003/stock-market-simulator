package org.example.market.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class FinancialInstrument {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private List<BigDecimal> historicalPrices;

    public FinancialInstrument(String symbol, String name, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.historicalPrices = new ArrayList<>(); // Inicjalizacja pustej listy
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

    public List<BigDecimal> getHistoricalPrices() {
        return historicalPrices;
    }

    public void setHistoricalPrices(List<BigDecimal> historicalPrices) {
        this.historicalPrices = historicalPrices;
    }
}
