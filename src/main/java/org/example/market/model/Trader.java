package org.example.market.model;

import java.math.BigDecimal;

public abstract class Trader extends User {
    private int riskAssessmentLevel; // 1-10
    private Portfolio portfolio;
    private BigDecimal budget;

    public Trader(String userId, String username, String password, String email, int riskAssessmentLevel) {
        super(userId, username, password, email);
        this.riskAssessmentLevel = riskAssessmentLevel;
        this.portfolio = new Portfolio();
        this.budget = new BigDecimal("10000.00"); // Początkowy budżet
    }

    public abstract boolean buy(FinancialInstrument instrument, BigDecimal quantity);

    public abstract boolean sell(FinancialInstrument instrument, BigDecimal quantity);

    public BigDecimal getBudget() {
        return budget;
    }

    protected void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public int assessRisk(Transaction transaction) {
        // Logika oceny ryzyka
        return 5; // przykładowa wartość
    }

    public boolean isAcceptableRisk(int riskLevel) {
        return riskLevel <= riskAssessmentLevel;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}
