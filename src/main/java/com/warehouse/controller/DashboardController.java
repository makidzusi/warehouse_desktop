package com.warehouse.controller;

import com.warehouse.model.Product;
import com.warehouse.service.AnalyticsService;
import com.warehouse.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label todayTransactionsLabel;
    @FXML private Label totalOutgoingLabel;

    @FXML private LineChart<String, Number> movementChart;
    @FXML private BarChart<String, Number> topProductsChart;

    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> lowSkuCol;
    @FXML private TableColumn<Product, String> lowNameCol;
    @FXML private TableColumn<Product, String> lowCategoryCol;
    @FXML private TableColumn<Product, Integer> lowQtyCol;
    @FXML private TableColumn<Product, Integer> lowMinQtyCol;

    private final AnalyticsService analyticsService = ServiceFactory.getInstance().getAnalyticsService();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru", "RU"));

    @FXML
    public void initialize() {
        lowStockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        setupTable();
        refresh();
    }

    private void setupTable() {
        lowSkuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        lowNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        lowCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        lowQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        lowMinQtyCol.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));
    }

    private void refresh() {
        // Stats
        totalProductsLabel.setText(String.valueOf(analyticsService.getTotalProducts()));
        lowStockLabel.setText(String.valueOf(analyticsService.getLowStockCount()));
        todayTransactionsLabel.setText(String.valueOf(analyticsService.getTodayTransactions()));
        BigDecimal outgoing = analyticsService.getTotalOutgoingValue();
        totalOutgoingLabel.setText(nf.format(outgoing) + " ₽");

        // Low stock table
        var lowStock = ServiceFactory.getInstance().getProductService().getLowStock();
        lowStockTable.setItems(FXCollections.observableArrayList(lowStock));

        // Movement chart
        movementChart.getData().clear();
        XYChart.Series<String, Number> incomingSeries = new XYChart.Series<>();
        incomingSeries.setName("Приход");
        Map<String, Double> incoming = analyticsService.getDailyIncoming(30);
        incoming.forEach((d, v) -> incomingSeries.getData().add(new XYChart.Data<>(d, v)));

        XYChart.Series<String, Number> outgoingSeries = new XYChart.Series<>();
        outgoingSeries.setName("Расход");
        Map<String, Double> outgoingMap = analyticsService.getDailyOutgoing(30);
        outgoingMap.forEach((d, v) -> outgoingSeries.getData().add(new XYChart.Data<>(d, v)));

        movementChart.getData().addAll(incomingSeries, outgoingSeries);

        // Top products chart
        topProductsChart.getData().clear();
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        Map<String, Integer> top = analyticsService.getTopProducts(10);
        top.forEach((name, qty) -> {
            String label = name.length() > 15 ? name.substring(0, 12) + "..." : name;
            topSeries.getData().add(new XYChart.Data<>(label, qty));
        });
        topProductsChart.getData().add(topSeries);
    }
}
