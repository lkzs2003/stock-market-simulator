package org.example.market.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Trader extends User {
    private int riskAssessmentLevel;
    private Portfolio portfolio;
    private BigDecimal budget;

    public Trader(String userId, String username, String password, String email, int riskAssessmentLevel) {
        super(userId, username, password, email);
        this.riskAssessmentLevel = riskAssessmentLevel;
        this.portfolio = new Portfolio();
        this.budget = new BigDecimal("10000.00");
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
        FinancialInstrument instrument = transaction.getInstrument();
        BigDecimal quantity = transaction.getQuantity();
        BigDecimal transactionValue = instrument.getCurrentPrice().multiply(quantity);

        double var = calculateVaR(instrument);
        double sharpeRatio = calculateSharpeRatio(instrument);
        double beta = calculateBeta(instrument);
        double transactionPercentage = transactionValue.divide(getBudget(), BigDecimal.ROUND_HALF_UP).doubleValue();

        double riskScore = (var * 0.4) + (sharpeRatio * 0.3) + (beta * 0.2) + (transactionPercentage * 0.1);

        return (int) Math.round(riskScore * 10); // Przykładowa skala 1-10
    }

    public boolean isAcceptableRisk(int riskLevel) {
        return riskLevel <= riskAssessmentLevel;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    private double calculateVaR(FinancialInstrument instrument) {
        List<BigDecimal> returns = instrument.getHistoricalPrices().stream()
                .map(price -> price.divide(price, BigDecimal.ROUND_HALF_UP))
                .collect(Collectors.toList());

        returns.sort(BigDecimal::compareTo);
        int varIndex = (int) Math.floor(returns.size() * 0.05); // 5% quantile
        return returns.get(varIndex).doubleValue();
    }

    private double calculateSharpeRatio(FinancialInstrument instrument) {
        List<BigDecimal> prices = instrument.getHistoricalPrices();
        double mean = prices.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double standardDeviation = calculateVolatility(instrument);
        double riskFreeRate = 0.01; // Przykładowa wolna od ryzyka stopa zwrotu
        return (mean - riskFreeRate) / standardDeviation;
    }

    private double calculateBeta(FinancialInstrument instrument) {
        List<BigDecimal> returns = instrument.getHistoricalPrices().stream()
                .map(price -> price.divide(price, BigDecimal.ROUND_HALF_UP))
                .collect(Collectors.toList());

        double covariance = calculateCovariance(returns, getMarketReturns());
        double marketVariance = calculateVariance(getMarketReturns());
        return covariance / marketVariance;
    }

    private double calculateCovariance(List<BigDecimal> returns, List<BigDecimal> marketReturns) {
        double meanReturns = returns.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double meanMarketReturns = marketReturns.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);

        double covariance = 0.0;
        for (int i = 0; i < returns.size(); i++) {
            covariance += (returns.get(i).doubleValue() - meanReturns) * (marketReturns.get(i).doubleValue() - meanMarketReturns);
        }
        return covariance / returns.size();
    }

    private double calculateVariance(List<BigDecimal> returns) {
        double mean = returns.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(price -> Math.pow(price.doubleValue() - mean, 2)).average().orElse(0.0);
        return variance;
    }

    private List<BigDecimal> getMarketReturns() {
        // Przykładowe dane rynkowe
        return List.of(
                new BigDecimal("0.01"), new BigDecimal("0.02"), new BigDecimal("0.01"), new BigDecimal("0.03"), new BigDecimal("0.02")
        );
    }

    private double calculateVolatility(FinancialInstrument instrument) {
        List<BigDecimal> prices = instrument.getHistoricalPrices();
        double mean = prices.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double variance = prices.stream().mapToDouble(price -> Math.pow(price.doubleValue() - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    @Override
    public boolean login(String username, String password) {
        return super.login(username, password);
    }
}
