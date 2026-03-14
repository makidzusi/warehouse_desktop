package com.warehouse;

import com.warehouse.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize DB on startup
        DatabaseManager.getInstance();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/warehouse/fxml/login.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 480, 600);
        scene.getStylesheets().add(
            getClass().getResource("/com/warehouse/css/styles.css").toExternalForm()
        );

        primaryStage.setTitle("Склад — Вход");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
