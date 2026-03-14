package com.warehouse.controller;

import com.warehouse.service.ServiceFactory;
import com.warehouse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label userInfoLabel;
    @FXML private Button navDashboard;
    @FXML private Button navProducts;
    @FXML private Button navCategories;
    @FXML private Button navTransactions;
    @FXML private Button navAnalytics;
    @FXML private Button navUsers;

    private final SessionManager session = SessionManager.getInstance();
    private Button activeNav;

    @FXML
    public void initialize() {
        var user = session.getCurrentUser();
        if (user != null) {
            userInfoLabel.setText(user.getFullName() + "\n" + user.getRole().getDisplayName());
        }

        // Hide users tab for non-admins
        navUsers.setVisible(session.isAdmin());
        navUsers.setManaged(session.isAdmin());

        activeNav = navDashboard;
        showDashboard();
    }

    @FXML public void showDashboard() { loadPage("/com/warehouse/fxml/dashboard.fxml", navDashboard); }
    @FXML public void showProducts()  { loadPage("/com/warehouse/fxml/products.fxml", navProducts); }
    @FXML public void showCategories(){ loadPage("/com/warehouse/fxml/categories.fxml", navCategories); }
    @FXML public void showTransactions(){ loadPage("/com/warehouse/fxml/transactions.fxml", navTransactions); }
    @FXML public void showAnalytics() { loadPage("/com/warehouse/fxml/analytics.fxml", navAnalytics); }
    @FXML public void showUsers()     { loadPage("/com/warehouse/fxml/users.fxml", navUsers); }

    private void loadPage(String fxmlPath, Button navBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();
            contentArea.getChildren().setAll(page);

            // Update active nav style
            if (activeNav != null) {
                activeNav.getStyleClass().remove("nav-btn-active");
            }
            navBtn.getStyleClass().add("nav-btn-active");
            activeNav = navBtn;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogout() {
        session.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/warehouse/fxml/login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root, 480, 600);
            scene.getStylesheets().add(
                getClass().getResource("/com/warehouse/css/styles.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Склад — Вход");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
