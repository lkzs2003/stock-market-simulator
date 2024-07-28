package org.example.market.simulation;

import org.example.market.data.StockDataLoader;
import org.example.market.data.StockDataPoint;
import org.example.market.GUI.MarketGUI;
import org.example.market.model.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Path to processed data files
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

        // Load market data
        StockDataLoader dataLoader = new StockDataLoader();
        List<StockDataPoint> dataPoints = dataLoader.loadMultipleStockData(filePaths);

        // Initialize market and financial instruments
        Market market = new Market();
        market.addInstrumentsFromSymbols(Arrays.asList("AAPL", "DELL", "MCD", "MSFT", "NFLX", "NKE", "SBUX", "TSLA"));

        // Add currencies as instances of Currency class
        market.addInstrument(new Currency("EUR=X", "Euro", BigDecimal.ZERO, "Eurozone"));
        market.addInstrument(new Currency("GBP=X", "Pound", BigDecimal.ZERO, "United Kingdom"));

        // Initialize trader
        Trader trader = new StockTrader("1", "JohnDoe", "password", "john.doe@example.com", 10000);

        // Initialize simulator and GUI
        MarketSimulator simulator = new MarketSimulator(dataPoints, market, null);
        MarketGUI marketGUI = new MarketGUI(market, trader, simulator);
        simulator.setMarketGUI(marketGUI);

        // Show GUI
        marketGUI.show();

        // Start simulation
        simulator.startSimulation();
    }
}
