package com.warehouse.controller;

import com.warehouse.service.AnalyticsService;
import com.warehouse.service.CategoryService;
import com.warehouse.service.ProductService;
import com.warehouse.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsController {

    @FXML private ComboBox<String> periodCombo;
    @FXML private Label totalIncomingLabel;
    @FXML private Label totalOutgoingLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;

    @FXML private AreaChart<String, Number> areaChart;
    @FXML private BarChart<String, Number> topBarChart;
    @FXML private PieChart categoryPieChart;

    private final AnalyticsService analyticsService = ServiceFactory.getInstance().getAnalyticsService();
    private final ProductService productService = ServiceFactory.getInstance().getProductService();
    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru", "RU"));

    @FXML
    public void initialize() {
        periodCombo.setItems(FXCollections.observableArrayList(
            "7 дней", "30 дней", "90 дней", "365 дней"
        ));
        periodCombo.setValue("30 дней");
        refresh();
    }

    @FXML
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        int days = parsePeriod(periodCombo.getValue());

        // Summary
        BigDecimal incoming = analyticsService.getTotalIncomingValue();
        BigDecimal outgoing = analyticsService.getTotalOutgoingValue();
        totalIncomingLabel.setText(nf.format(incoming) + " ₽");
        totalOutgoingLabel.setText(nf.format(outgoing) + " ₽");
        totalProductsLabel.setText(String.valueOf(analyticsService.getTotalProducts()));
        lowStockLabel.setText(String.valueOf(analyticsService.getLowStockCount()));

        // Area chart
        areaChart.getData().clear();
        XYChart.Series<String, Number> inSeries = new XYChart.Series<>();
        inSeries.setName("Приход");
        analyticsService.getDailyIncoming(days)
            .forEach((d, v) -> inSeries.getData().add(new XYChart.Data<>(d, v)));

        XYChart.Series<String, Number> outSeries = new XYChart.Series<>();
        outSeries.setName("Расход");
        analyticsService.getDailyOutgoing(days)
            .forEach((d, v) -> outSeries.getData().add(new XYChart.Data<>(d, v)));

        areaChart.getData().addAll(inSeries, outSeries);

        // Top products bar
        topBarChart.getData().clear();
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        analyticsService.getTopProducts(10).forEach((name, qty) -> {
            String label = name.length() > 15 ? name.substring(0, 12) + "..." : name;
            topSeries.getData().add(new XYChart.Data<>(label, qty));
        });
        topBarChart.getData().add(topSeries);

        // Pie chart by category count of products
        categoryPieChart.getData().clear();
        var products = productService.getAll();
        var categoryMap = new java.util.HashMap<String, Integer>();
        for (var p : products) {
            String cat = p.getCategoryName() != null ? p.getCategoryName() : "Без категории";
            categoryMap.merge(cat, 1, Integer::sum);
        }
        categoryMap.forEach((cat, count) ->
            categoryPieChart.getData().add(new PieChart.Data(cat + " (" + count + ")", count))
        );
    }

    private int parsePeriod(String s) {
        if (s == null) return 30;
        return switch (s) {
            case "7 дней"   -> 7;
            case "90 дней"  -> 90;
            case "365 дней" -> 365;
            default         -> 30;
        };
    }
}
