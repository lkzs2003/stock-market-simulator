package org.example.market.model;

import java.math.BigDecimal;

public class Currency extends FinancialInstrument {
    private final String country;

    public Currency(String symbol, String name, BigDecimal currentPrice, String country) {
        super(symbol, name, currentPrice);
        this.country = country;
    }

    public String getCountry() {
        return this.country;
    }
}

