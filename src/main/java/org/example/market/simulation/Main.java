package org.example.market.simulation;

import org.example.market.GUI.LoginFrame;
import org.example.market.data.StockDataLoader;
import org.example.market.data.StockDataPoint;
import org.example.market.model.Market;
import org.example.market.service.UserService;
import org.example.market.model.Currency;
import org.example.market.model.Stock;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // Paths to CSV files containing stock data
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

        // Load stock data from CSV files
        StockDataLoader dataLoader = new StockDataLoader();
        List<StockDataPoint> dataPoints = dataLoader.loadMultipleStockData(filePaths);

        // Group stock data points by symbol
        Map<String, List<StockDataPoint>> dataPointsBySymbol = dataPoints.stream()
                .collect(Collectors.groupingBy(StockDataPoint::getSymbol));

        // Initialize the market with stock data
        Market market = initializeMarket(dataPointsBySymbol);

        // Creating a UserService instance to manage users
        UserService userService = new UserService();

        // Start the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(userService, market, dataPoints);
            loginFrame.setVisible(true);
        });
    }

    private static Market initializeMarket(Map<String, List<StockDataPoint>> dataPointsBySymbol) {
        Market market = new Market();
        List<String> stockSymbols = Arrays.asList("AAPL", "DELL", "MCD", "MSFT", "NFLX", "NKE", "SBUX", "TSLA");

        // Adding stocks to the market
        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol, symbol + " Inc.", BigDecimal.ZERO, "NASDAQ");
            stock.setHistoricalPrices(dataPointsBySymbol.get(symbol).stream()
                    .map(StockDataPoint::getPrice)
                    .collect(Collectors.toList()));
            market.addInstrument(stock);
        }

        // Adding currencies to the market
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
