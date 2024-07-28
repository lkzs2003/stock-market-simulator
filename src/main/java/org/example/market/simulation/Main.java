package org.example.market.simulation;

import org.example.market.data.StockDataLoader;
import org.example.market.data.StockDataPoint;
import org.example.market.GUI.MarketGUI;
import org.example.market.model.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<String> filePaths = Arrays.asList(
                "AAPL.csv",
                "DELL.csv",
                "EUR=X.csv",
                "GBP=X.csv",
                "MCD.csv",
                "MSFT.csv",
                "NFLX.csv",
                "NKE.csv",
                "SBUX.csv",
                "TSLA.csv"
        );

        StockDataLoader dataLoader = new StockDataLoader();
        List<StockDataPoint> dataPoints = dataLoader.loadMultipleStockData(filePaths);

        Map<String, List<StockDataPoint>> dataPointsBySymbol = dataPoints.stream()
                .collect(Collectors.groupingBy(StockDataPoint::getSymbol));

        Market market = initializeMarket(dataPointsBySymbol);

        Trader trader = new StockTrader("1", "JohnDoe", "password", "john.doe@example.com", 5);

        MarketSimulator simulator = new MarketSimulator(dataPoints, market, null);
        MarketGUI marketGUI = new MarketGUI(market, trader, simulator);
        simulator.setMarketGUI(marketGUI);

        marketGUI.show();
        simulator.startSimulation();
    }

    private static Market initializeMarket(Map<String, List<StockDataPoint>> dataPointsBySymbol) {
        Market market = new Market();
        List<String> stockSymbols = Arrays.asList("AAPL", "DELL", "MCD", "MSFT", "NFLX", "NKE", "SBUX", "TSLA");

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol, symbol + " Inc.", BigDecimal.ZERO, "NASDAQ");
            stock.setHistoricalPrices(dataPointsBySymbol.get(symbol).stream()
                    .map(StockDataPoint::getPrice)
                    .collect(Collectors.toList()));
            market.addInstrument(stock);
        }

        Currency eur = new Currency("EUR=X", "Euro", BigDecimal.ZERO, "Eurozone");
        eur.setHistoricalPrices(dataPointsBySymbol.get("EUR=X").stream()
                .map(StockDataPoint::getPrice)
                .collect(Collectors.toList()));
        market.addInstrument(eur);

        Currency gbp = new Currency("GBP=X", "Pound", BigDecimal.ZERO, "United Kingdom");
        gbp.setHistoricalPrices(dataPointsBySymbol.get("GBP=X").stream()
                .map(StockDataPoint::getPrice)
                .collect(Collectors.toList()));
        market.addInstrument(gbp);

        return market;
    }
}
