package com.warehouse.controller;

import com.warehouse.model.User;
import com.warehouse.service.AuthService;
import com.warehouse.service.ServiceFactory;
import com.warehouse.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, Void> colActions;

    private final AuthService authService = ServiceFactory.getInstance().getAuthService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getId()).asObject());
        colUsername.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUsername()));
        colFullName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFullName()));
        colRole.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRole().getDisplayName()));
        colActive.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isActive()));
        colActive.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v ? "Да" : "Нет");
                setStyle(v ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            }
        });
        colCreatedAt.setCellValueFactory(cd -> {
            var dt = cd.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(dtf) : "");
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Изменить");
            private final Button passBtn = new Button("Пароль");
            private final HBox box = new HBox(6, editBtn, passBtn);
            {
                editBtn.getStyleClass().add("btn-small");
                passBtn.getStyleClass().addAll("btn-small", "btn-secondary-small");
                editBtn.setOnAction(e -> onEdit(getTableRow().getItem()));
                passBtn.setOnAction(e -> onChangePassword(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        loadData();
    }

    private void loadData() {
        usersTable.setItems(FXCollections.observableArrayList(authService.getAllUsers()));
    }

    @FXML
    private void onAdd() {
        showUserDialog(null);
    }

    private void onEdit(User user) {
        if (user != null) showUserDialog(user);
    }

    private void onChangePassword(User user) {
        if (user == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Смена пароля: " + user.getFullName());
        dialog.initOwner(usersTable.getScene().getWindow());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(240);
        PasswordField confirmField = new PasswordField();
        confirmField.setPrefWidth(240);
        grid.add(new Label("Новый пароль *"), 0, 0);
        grid.add(passField, 1, 0);
        grid.add(new Label("Подтверждение *"), 0, 1);
        grid.add(confirmField, 1, 1);

        DialogPane dp = dialog.getDialogPane();
        dp.setContent(grid);
        dp.getStylesheets().add(getClass().getResource("/com/warehouse/css/styles.css").toExternalForm());
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                String p1 = passField.getText();
                String p2 = confirmField.getText();
                if (p1.length() < 4) { AlertUtil.showError("Ошибка", "Пароль должен быть не менее 4 символов"); return; }
                if (!p1.equals(p2)) { AlertUtil.showError("Ошибка", "Пароли не совпадают"); return; }
                authService.changePassword(user.getId(), p1);
                AlertUtil.showInfo("Успех", "Пароль изменён");
            }
        });
    }

    private void showUserDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Новый пользователь" : "Редактировать пользователя");
        dialog.initOwner(usersTable.getScene().getWindow());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        TextField loginField = new TextField();
        loginField.setPrefWidth(240);
        TextField fullNameField = new TextField();
        fullNameField.setPrefWidth(240);
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(240);
        ComboBox<User.Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(User.Role.values()));
        roleCombo.setPrefWidth(240);
        CheckBox activeCheck = new CheckBox("Активен");

        if (user != null) {
            loginField.setText(user.getUsername());
            loginField.setDisable(true);
            fullNameField.setText(user.getFullName());
            roleCombo.setValue(user.getRole());
            activeCheck.setSelected(user.isActive());
        } else {
            roleCombo.setValue(User.Role.MANAGER);
            activeCheck.setSelected(true);
        }

        roleCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User.Role r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "" : r.getDisplayName());
            }
        });
        roleCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(User.Role r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "" : r.getDisplayName());
            }
        });

        grid.add(new Label("Логин *"), 0, 0);     grid.add(loginField, 1, 0);
        grid.add(new Label("Полное имя *"), 0, 1); grid.add(fullNameField, 1, 1);
        if (user == null) {
            grid.add(new Label("Пароль *"), 0, 2);
            grid.add(passField, 1, 2);
            grid.add(new Label("Роль"), 0, 3);    grid.add(roleCombo, 1, 3);
            grid.add(activeCheck, 1, 4);
        } else {
            grid.add(new Label("Роль"), 0, 2);    grid.add(roleCombo, 1, 2);
            grid.add(activeCheck, 1, 3);
        }

        DialogPane dp = dialog.getDialogPane();
        dp.setContent(grid);
        dp.getStylesheets().add(getClass().getResource("/com/warehouse/css/styles.css").toExternalForm());
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (user == null) {
                    String pass = passField.getText();
                    if (pass.length() < 4) throw new IllegalArgumentException("Пароль должен быть не менее 4 символов");
                    authService.register(loginField.getText().trim(), pass,
                        fullNameField.getText().trim(), roleCombo.getValue());
                } else {
                    user.setFullName(fullNameField.getText().trim());
                    user.setRole(roleCombo.getValue());
                    user.setActive(activeCheck.isSelected());
                    authService.updateUser(user);
                }
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Ошибка", e.getMessage());
            }
        }
    }
}
