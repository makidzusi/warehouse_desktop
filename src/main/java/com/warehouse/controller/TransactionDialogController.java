package com.warehouse.controller;

import com.warehouse.model.Product;
import com.warehouse.model.Transaction;
import com.warehouse.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionDialogController {

    @FXML private ComboBox<Product> productCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TextField priceField;
    @FXML private Label counterpartyLabel;
    @FXML private TextField counterpartyField;
    @FXML private TextArea noteArea;
    @FXML private Label totalLabel;

    private Transaction.Type type;
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru", "RU"));

    @FXML
    public void initialize() {
        quantitySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999999, 1)
        );
        quantitySpinner.valueProperty().addListener((o, ov, nv) -> updateTotal());
        priceField.textProperty().addListener((o, ov, nv) -> updateTotal());

        productCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName() + " (" + p.getSku() + ") — остаток: " + p.getQuantity());
            }
        });
        productCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName() + " — " + p.getQuantity() + " " + (p.getUnit() != null ? p.getUnit() : "шт"));
            }
        });
        productCombo.setOnAction(e -> {
            Product p = productCombo.getValue();
            if (p != null && priceField.getText().isBlank()) {
                priceField.setText(p.getPrice().toPlainString());
                updateTotal();
            }
        });
    }

    public void setType(Transaction.Type type) {
        this.type = type;
        counterpartyLabel.setText(
            type == Transaction.Type.INCOMING ? "Поставщик" :
            type == Transaction.Type.OUTGOING ? "Покупатель" : "Причина"
        );
        counterpartyField.setPromptText(
            type == Transaction.Type.INCOMING ? "Введите поставщика" :
            type == Transaction.Type.OUTGOING ? "Введите покупателя" : "Причина корректировки"
        );
        if (type == Transaction.Type.ADJUSTMENT) {
            priceField.setDisable(true);
            priceField.setText("0");
            quantitySpinner.getValueFactory().setValue(0);
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) quantitySpinner.getValueFactory()).setMin(0);
        }
    }

    public void setProducts(List<Product> products) {
        productCombo.setItems(FXCollections.observableArrayList(products));
    }

    private void updateTotal() {
        try {
            int qty = quantitySpinner.getValue();
            BigDecimal price = new BigDecimal(priceField.getText().trim().replace(",", "."));
            BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
            totalLabel.setText(nf.format(total) + " ₽");
        } catch (NumberFormatException e) {
            totalLabel.setText("—");
        }
    }

    public void execute(TransactionService service) {
        Product product = productCombo.getValue();
        if (product == null) throw new IllegalArgumentException("Выберите товар");

        int qty = quantitySpinner.getValue();
        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            price = BigDecimal.ZERO;
        }

        String counterparty = counterpartyField.getText().trim();
        String note = noteArea.getText().trim();

        switch (type) {
            case INCOMING  -> service.createIncoming(product.getId(), qty, price, counterparty, note);
            case OUTGOING  -> service.createOutgoing(product.getId(), qty, price, counterparty, note);
            case ADJUSTMENT -> service.createAdjustment(product.getId(), qty, note);
        }
    }
}
