package org.example.market.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Market {
    private List<FinancialInstrument> instruments;

    public Market() {
        this.instruments = new ArrayList<>();
    }

    public void addInstrument(FinancialInstrument instrument) {
        this.instruments.add(instrument);
    }

    public void addInstrumentsFromSymbols(List<String> symbols) {
        for (String symbol : symbols) {
            this.instruments.add(new Stock(symbol, symbol + " Inc.", BigDecimal.ZERO, "NASDAQ"));
        }
    }

    public List<FinancialInstrument> getInstruments() {
        return instruments;
    }

    public FinancialInstrument getInstrument(String symbol) {
        for (FinancialInstrument instrument : instruments) {
            if (instrument.getSymbol().equals(symbol)) {
                return instrument;
            }
        }
        return null;
    }
}

