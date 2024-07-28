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
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {

            if (is == null) throw new IOException("Resource not found: " + filePath);
            reader.readNext(); // Skip header

            String symbol = extractSymbol(filePath);
            String[] line;
            while ((line = reader.readNext()) != null) {
                dataPoints.add(parseStockDataPoint(line, symbol));
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return dataPoints;
    }

    private String extractSymbol(String filePath) {
        return filePath.split("/")[filePath.split("/").length - 1].split("\\.")[0];
    }

    private StockDataPoint parseStockDataPoint(String[] line, String symbol) {
        LocalDate date = LocalDate.parse(line[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        BigDecimal openPrice = new BigDecimal(line[1]);
        return new StockDataPoint(date.atStartOfDay(), openPrice, symbol);
    }

    public List<StockDataPoint> loadMultipleStockData(List<String> filePaths) {
        List<StockDataPoint> allDataPoints = new ArrayList<>();
        for (String filePath : filePaths) {
            allDataPoints.addAll(loadStockData(filePath));
        }
        return allDataPoints;
    }
}
