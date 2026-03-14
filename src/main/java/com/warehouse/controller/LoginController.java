package com.warehouse.controller;

import com.warehouse.service.AuthService;
import com.warehouse.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = ServiceFactory.getInstance().getAuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) onLogin();
        });
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) passwordField.requestFocus();
        });
        // Create default admin if no users
        authService.createDefaultAdmin();
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Введите логин и пароль");
            return;
        }

        loginButton.setDisable(true);
        try {
            if (authService.login(username, password)) {
                openMainWindow();
            } else {
                showError("Неверный логин или пароль");
                passwordField.clear();
            }
        } catch (Exception e) {
            showError("Ошибка: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/warehouse/fxml/main.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(
                getClass().getResource("/com/warehouse/css/styles.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Склад — Система управления");
            stage.setResizable(true);
            stage.setWidth(1280);
            stage.setHeight(800);
            stage.setMinWidth(1024);
            stage.setMinHeight(680);
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Ошибка открытия приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
