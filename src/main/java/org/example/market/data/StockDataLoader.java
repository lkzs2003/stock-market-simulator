package org.example.market.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StockDataLoader {

    public List<StockDataPoint> loadStockData(String filePath) {
        List<StockDataPoint> dataPoints = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + filePath);
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
                String[] line;
                reader.readNext(); // Skip header
                String symbol = filePath.split("/")[filePath.split("/").length - 1].split("\\.")[0]; // Extract symbol from file name
                System.out.println("Loading data for symbol: " + symbol);
                while ((line = reader.readNext()) != null) {
                    String date = line[0];
                    BigDecimal openPrice = new BigDecimal(line[1]); // Use open price

                    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDateTime dateTime = localDate.atStartOfDay(); // Use start of the day

                    StockDataPoint dataPoint = new StockDataPoint(dateTime, openPrice, symbol);
                    dataPoints.add(dataPoint);
                }
            } catch (CsvValidationException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Debug: Print loaded data
        System.out.println("Loaded data points for " + filePath + ":");
        for (StockDataPoint dataPoint : dataPoints) {
            System.out.println(dataPoint.getSymbol() + ": " + dataPoint.getDateTime() + " - " + dataPoint.getPrice());
        }

        return dataPoints;
    }

    public List<StockDataPoint> loadMultipleStockData(List<String> filePaths) {
        List<StockDataPoint> allDataPoints = new ArrayList<>();
        for (String filePath : filePaths) {
            List<StockDataPoint> dataPoints = loadStockData(filePath);
            System.out.println("Data points loaded for " + filePath + ": " + dataPoints.size());
            allDataPoints.addAll(dataPoints);
        }
        System.out.println("Total data points loaded: " + allDataPoints.size());
        return allDataPoints;
    }
}
