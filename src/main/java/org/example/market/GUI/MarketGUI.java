package org.example.market.GUI;

import java.math.BigDecimal;
import java.util.Map;
import org.jfree.data.xy.XYSeries;

public interface MarketGUI {
    void updateCurrentPrice(String symbol, BigDecimal price);
    void updateChart(String symbol, BigDecimal price, long elapsedTime);
    Map<String, XYSeries> getSeriesMap();
}