package org.example.market.model;

import java.math.BigDecimal;

public class StockTrader extends Trader {
    public StockTrader(String userId, String username, String password, String email, int riskAssessmentLevel) {
        super(userId, username, password, email, riskAssessmentLevel);
    }

    @Override
    public boolean buy(FinancialInstrument instrument, BigDecimal quantity) {
        BigDecimal totalCost = instrument.getCurrentPrice().multiply(quantity);
        if (totalCost.compareTo(getBudget()) <= 0) {
            setBudget(getBudget().subtract(totalCost));
            getPortfolio().addInstrument(instrument, quantity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sell(FinancialInstrument instrument, BigDecimal quantity) {
        BigDecimal portfolioQuantity = getPortfolio().getQuantity(instrument);
        if (portfolioQuantity.compareTo(quantity) >= 0) {
            BigDecimal totalRevenue = instrument.getCurrentPrice().multiply(quantity);
            setBudget(getBudget().add(totalRevenue));
            getPortfolio().removeInstrument(instrument, quantity);
            return true;
        } else {
            return false;
        }
    }
}
