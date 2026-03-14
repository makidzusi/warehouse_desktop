package com.warehouse.controller;

import com.warehouse.model.Category;
import com.warehouse.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class ProductDialogController {

    @FXML private TextField nameField;
    @FXML private TextField skuField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Spinner<Integer> minQtySpinner;
    @FXML private TextArea descriptionArea;

    private static final List<String> UNITS = List.of(
        "шт", "кг", "г", "л", "м", "м²", "м³", "упак", "рул", "пара", "компл"
    );

    @FXML
    public void initialize() {
        unitCombo.setItems(FXCollections.observableArrayList(UNITS));
        unitCombo.setValue("шт");
        quantitySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0)
        );
        minQtySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0)
        );
    }

    public void setCategories(List<Category> categories) {
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
    }

    public void setProduct(Product product) {
        nameField.setText(product.getName());
        skuField.setText(product.getSku());
        priceField.setText(product.getPrice() != null ? product.getPrice().toPlainString() : "0");
        unitCombo.setValue(product.getUnit() != null ? product.getUnit() : "шт");
        quantitySpinner.getValueFactory().setValue(product.getQuantity());
        minQtySpinner.getValueFactory().setValue(product.getMinQuantity());
        if (product.getDescription() != null) descriptionArea.setText(product.getDescription());
        if (product.getCategoryId() > 0) {
            categoryCombo.getItems().stream()
                .filter(c -> c.getId() == product.getCategoryId())
                .findFirst()
                .ifPresent(categoryCombo::setValue);
        }
    }

    public Product getResult() {
        Product p = new Product();
        p.setName(nameField.getText().trim());
        p.setSku(skuField.getText().trim());
        p.setDescription(descriptionArea.getText().trim());
        p.setUnit(unitCombo.getValue());
        p.setQuantity(quantitySpinner.getValue());
        p.setMinQuantity(minQtySpinner.getValue());
        try {
            p.setPrice(new BigDecimal(priceField.getText().trim().replace(",", ".")));
        } catch (NumberFormatException e) {
            p.setPrice(BigDecimal.ZERO);
        }
        Category cat = categoryCombo.getValue();
        if (cat != null) p.setCategoryId(cat.getId());
        return p;
    }
}
