package org.example.market.GUI;

import java.math.BigDecimal;

public interface MarketGUI {
    void updateCurrentPrice(String symbol, BigDecimal price);
    void updateChart(String symbol, BigDecimal price, long elapsedTime);
    String getCurrentInstrument();
}
