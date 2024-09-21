package org.example.market.model;

import java.math.BigDecimal;

public class StockTrader extends Trader {
    public StockTrader(String userId, String username, String password, String email) {
        super(userId, username, password, email);
    }

    @Override
    public boolean buy(FinancialInstrument instrument, BigDecimal quantity) {
        BigDecimal totalCost = instrument.getCurrentPrice().multiply(quantity);
        if (totalCost.compareTo(getBudget()) <= 0) { // Check if the trader has enough budget
            setBudget(getBudget().subtract(totalCost));  // Deduct the total cost from the budget
            getPortfolio().addInstrument(instrument, quantity);  // Add the instrument to the portfolio
            return true;
        } else {
            return false;  // Not enough budget to buy the instrument
        }
    }

    @Override
    public boolean sell(FinancialInstrument instrument, BigDecimal quantity) {
        BigDecimal portfolioQuantity = getPortfolio().getQuantity(instrument);
        if (portfolioQuantity.compareTo(quantity) >= 0) {  // Check if the trader has enough of the instrument
            BigDecimal totalRevenue = instrument.getCurrentPrice().multiply(quantity);
            setBudget(getBudget().add(totalRevenue));  // Add the revenue to the trader's budget
            getPortfolio().removeInstrument(instrument, quantity);  // Remove the instrument from the portfolio
            return true;
        } else {
            return false;  // Not enough quantity to sell
        }
    }
}
