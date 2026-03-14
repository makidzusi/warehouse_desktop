package com.warehouse.controller;

import com.warehouse.model.Category;
import com.warehouse.service.CategoryService;
import com.warehouse.service.ServiceFactory;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class CategoryController {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TableColumn<Category, Void> colActions;

    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();

    @FXML
    public void initialize() {
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getId()).asObject());
        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colDescription.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Изменить");
            private final Button deleteBtn = new Button("Удалить");
            private final HBox box = new HBox(6, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("btn-small");
                deleteBtn.getStyleClass().addAll("btn-small", "btn-danger-small");
                boolean canEdit = SessionManager.getInstance().isManager();
                editBtn.setDisable(!canEdit);
                deleteBtn.setDisable(!canEdit);
                editBtn.setOnAction(e -> onEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> onDelete(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        loadData();
    }

    private void loadData() {
        categoriesTable.setItems(FXCollections.observableArrayList(categoryService.getAll()));
    }

    @FXML
    private void onAdd() {
        showDialog(null);
    }

    private void onEdit(Category category) {
        if (category != null) showDialog(category);
    }

    private void onDelete(Category category) {
        if (category == null) return;
        if (AlertUtil.showConfirmation("Удаление", "Удалить категорию \"" + category.getName() + "\"?")) {
            try {
                categoryService.delete(category.getId());
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Ошибка", e.getMessage());
            }
        }
    }

    private void showDialog(Category category) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "Добавить категорию" : "Редактировать категорию");
        dialog.setHeaderText(null);
        dialog.initOwner(categoriesTable.getScene().getWindow());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        TextField nameField = new TextField();
        nameField.setPrefWidth(280);
        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(280);

        if (category != null) {
            nameField.setText(category.getName());
            if (category.getDescription() != null) descArea.setText(category.getDescription());
        }

        grid.add(new Label("Название *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание"), 0, 1);
        grid.add(descArea, 1, 1);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(grid);
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/warehouse/css/styles.css").toExternalForm()
        );
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { AlertUtil.showError("Ошибка", "Название не может быть пустым"); return; }
            try {
                Category c = category != null ? category : new Category();
                c.setName(name);
                c.setDescription(descArea.getText().trim());
                categoryService.save(c);
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Ошибка сохранения", e.getMessage());
            }
        }
    }
}
