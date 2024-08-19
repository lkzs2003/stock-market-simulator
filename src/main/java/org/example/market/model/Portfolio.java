package org.example.market.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private Map<FinancialInstrument, BigDecimal> instruments;

    public Portfolio() {
        this.instruments = new HashMap<>();
    }

    public void addInstrument(FinancialInstrument instrument, BigDecimal quantity) {
        instruments.merge(instrument, quantity, BigDecimal::add);
    }

    public void removeInstrument(FinancialInstrument instrument, BigDecimal quantity) {
        instruments.computeIfPresent(instrument, (key, oldQuantity) -> oldQuantity.subtract(quantity));
        instruments.remove(instrument, BigDecimal.ZERO);
    }

    public BigDecimal getQuantity(FinancialInstrument instrument) {
        return instruments.getOrDefault(instrument, BigDecimal.ZERO);
    }

    public Map<FinancialInstrument, BigDecimal> getInstruments() {
        return instruments;
    }
}
