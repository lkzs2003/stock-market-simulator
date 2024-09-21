package org.example.market.model;

import java.math.BigDecimal;

public abstract class Trader extends User {
    private Portfolio portfolio;
    private BigDecimal budget;

    public Trader(String userId, String username, String password, String email) {
        super(userId, username, password, email);
        this.portfolio = new Portfolio();
        this.budget = new BigDecimal("10000.00");
    }

    public abstract boolean buy(FinancialInstrument instrument, BigDecimal quantity);

    public abstract boolean sell(FinancialInstrument instrument, BigDecimal quantity);

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }


    public Portfolio getPortfolio() {
        return portfolio;
    }

    @Override
    public boolean login(String username, String password) {
        return super.login(username, password);
    }
}
