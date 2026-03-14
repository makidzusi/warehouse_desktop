package com.warehouse.controller;

import com.warehouse.model.Product;
import com.warehouse.service.CategoryService;
import com.warehouse.service.ProductService;
import com.warehouse.service.ServiceFactory;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProductController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colSku;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Integer> colQuantity;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, Integer> colMinQty;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Void> colActions;

    private final ProductService productService = ServiceFactory.getInstance().getProductService();
    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru", "RU"));

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadData();
    }

    private void setupColumns() {
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colMinQty.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));

        colPrice.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPrice() != null ? nf.format(cd.getValue().getPrice()) + " ₽" : "-"
            )
        );

        colStatus.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().isLowStock() ? "Мало на складе" : "В норме"
            )
        );

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Мало на складе".equals(item)) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #27ae60;");
                }
            }
        });

        colActions.setCellFactory(buildActionsFactory());
    }

    private Callback<TableColumn<Product, Void>, TableCell<Product, Void>> buildActionsFactory() {
        return col -> new TableCell<>() {
            private final Button editBtn = new Button("Изменить");
            private final Button deleteBtn = new Button("Удалить");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-small");
                deleteBtn.getStyleClass().addAll("btn-small", "btn-danger-small");
                editBtn.setOnAction(e -> onEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> onDelete(getTableRow().getItem()));
                boolean canEdit = SessionManager.getInstance().isManager();
                editBtn.setDisable(!canEdit);
                deleteBtn.setDisable(!canEdit);
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, query) -> {
            List<Product> results = productService.search(query);
            productsTable.setItems(FXCollections.observableArrayList(results));
        });
    }

    private void loadData() {
        List<Product> products = productService.getAll();
        productsTable.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    private void onAdd() {
        showDialog(null);
    }

    private void onEdit(Product product) {
        if (product == null) return;
        showDialog(product);
    }

    private void onDelete(Product product) {
        if (product == null) return;
        if (AlertUtil.showConfirmation("Удаление товара",
                "Вы уверены, что хотите удалить товар \"" + product.getName() + "\"?")) {
            try {
                productService.delete(product.getId());
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Ошибка", e.getMessage());
            }
        }
    }

    private void showDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/warehouse/fxml/product_dialog.fxml")
            );
            DialogPane dialogPane = loader.load();
            ProductDialogController controller = loader.getController();
            controller.setCategories(categoryService.getAll());
            if (product != null) controller.setProduct(product);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(product == null ? "Добавить товар" : "Редактировать товар");
            dialog.setDialogPane(dialogPane);
            dialog.initOwner(productsTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    Product p = controller.getResult();
                    if (product != null) p.setId(product.getId());
                    productService.save(p);
                    loadData();
                } catch (Exception e) {
                    AlertUtil.showError("Ошибка сохранения", e.getMessage());
                }
            }
        } catch (IOException e) {
            AlertUtil.showError("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }
}
