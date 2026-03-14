package com.warehouse.controller;

import com.warehouse.model.Product;
import com.warehouse.model.Transaction;
import com.warehouse.service.ProductService;
import com.warehouse.service.ServiceFactory;
import com.warehouse.service.TransactionService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionController {

    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<Product> productFilter;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colProduct;
    @FXML private TableColumn<Transaction, Integer> colQty;
    @FXML private TableColumn<Transaction, String> colUnitPrice;
    @FXML private TableColumn<Transaction, String> colTotal;
    @FXML private TableColumn<Transaction, String> colCounterparty;
    @FXML private TableColumn<Transaction, String> colUser;
    @FXML private TableColumn<Transaction, String> colNote;

    private final TransactionService transactionService = ServiceFactory.getInstance().getTransactionService();
    private final ProductService productService = ServiceFactory.getInstance().getProductService();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private ObservableList<Transaction> allTransactions;

    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
        loadData();
    }

    private void setupColumns() {
        colDate.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getCreatedAt() != null
                ? cd.getValue().getCreatedAt().format(dtf) : ""));
        colType.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getType().getDisplayName()));
        colProduct.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getProductName()));
        colQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getUnitPrice() != null
                ? nf.format(cd.getValue().getUnitPrice()) + " ₽" : "—"));
        colTotal.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getTotalPrice() != null
                ? nf.format(cd.getValue().getTotalPrice()) + " ₽" : "—"));
        colCounterparty.setCellValueFactory(cd -> {
            Transaction t = cd.getValue();
            String cp = t.getType() == Transaction.Type.INCOMING ? t.getSupplier() : t.getCustomer();
            return new SimpleStringProperty(cp != null ? cp : "");
        });
        colUser.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getUserName() != null ? cd.getValue().getUserName() : ""));
        colNote.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getNote() != null ? cd.getValue().getNote() : ""));

        // Color rows by type
        transactionsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                switch (item.getType()) {
                    case INCOMING   -> setStyle("-fx-background-color: #f0fff4;");
                    case OUTGOING   -> setStyle("-fx-background-color: #fff5f5;");
                    case ADJUSTMENT -> setStyle("-fx-background-color: #fffbf0;");
                }
            }
        });
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList(
            "Все", "Приход", "Расход", "Корректировка"
        ));
        typeFilter.setValue("Все");

        List<Product> products = productService.getAll();
        productFilter.setItems(FXCollections.observableArrayList(products));
        productFilter.getItems().add(0, null);

        typeFilter.setOnAction(e -> applyFilters());
        productFilter.setOnAction(e -> applyFilters());

        productFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Все товары" : p.getName());
            }
        });
        productFilter.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Все товары" : p.getName());
            }
        });
    }

    private void loadData() {
        allTransactions = FXCollections.observableArrayList(transactionService.getAll());
        transactionsTable.setItems(allTransactions);
    }

    private void applyFilters() {
        if (allTransactions == null) return;
        List<Transaction> filtered = allTransactions.stream()
            .filter(t -> {
                String type = typeFilter.getValue();
                if (type == null || "Все".equals(type)) return true;
                return t.getType().getDisplayName().equals(type);
            })
            .filter(t -> {
                Product p = productFilter.getValue();
                if (p == null) return true;
                return t.getProductId() == p.getId();
            })
            .collect(Collectors.toList());
        transactionsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML private void onRefresh() { loadData(); applyFilters(); }
    @FXML private void onResetFilter() {
        typeFilter.setValue("Все");
        productFilter.setValue(null);
        transactionsTable.setItems(allTransactions);
    }

    @FXML private void onIncoming() {
        if (!SessionManager.getInstance().isManager()) {
            AlertUtil.showError("Доступ запрещён", "У вас нет прав для выполнения операций");
            return;
        }
        showTransactionDialog(Transaction.Type.INCOMING);
    }

    @FXML private void onOutgoing() {
        if (!SessionManager.getInstance().isManager()) {
            AlertUtil.showError("Доступ запрещён", "У вас нет прав для выполнения операций");
            return;
        }
        showTransactionDialog(Transaction.Type.OUTGOING);
    }

    @FXML private void onAdjustment() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError("Доступ запрещён", "Только администратор может делать корректировки");
            return;
        }
        showTransactionDialog(Transaction.Type.ADJUSTMENT);
    }

    private void showTransactionDialog(Transaction.Type type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/warehouse/fxml/transaction_dialog.fxml")
            );
            DialogPane dialogPane = loader.load();
            TransactionDialogController ctrl = loader.getController();
            ctrl.setType(type);
            ctrl.setProducts(productService.getAll());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(type.getDisplayName());
            dialog.setDialogPane(dialogPane);
            dialog.initOwner(transactionsTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    ctrl.execute(transactionService);
                    loadData();
                    AlertUtil.showInfo("Успех", "Операция выполнена успешно");
                } catch (Exception e) {
                    AlertUtil.showError("Ошибка", e.getMessage());
                }
            }
        } catch (IOException e) {
            AlertUtil.showError("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }
}
