package org.example.market.model;

import java.math.BigDecimal;

public class Stock extends FinancialInstrument {
    private final String stockExchange;

    public Stock(String symbol, String name, BigDecimal currentPrice, String stockExchange) {
        super(symbol, name, currentPrice);
        this.stockExchange = stockExchange;
    }
}
