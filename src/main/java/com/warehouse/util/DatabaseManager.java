package com.warehouse.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_FILE = "warehouse.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        connect();
        initSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            String url = "jdbc:sqlite:" + DB_FILE;
            connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к базе данных: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    private void initSchema() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'VIEWER',
                    active INTEGER NOT NULL DEFAULT 1,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    description TEXT
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    sku TEXT NOT NULL UNIQUE,
                    description TEXT,
                    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
                    quantity INTEGER NOT NULL DEFAULT 0,
                    min_quantity INTEGER NOT NULL DEFAULT 0,
                    price DECIMAL(12,2) NOT NULL DEFAULT 0,
                    unit TEXT NOT NULL DEFAULT 'шт'
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    product_id INTEGER NOT NULL REFERENCES products(id),
                    quantity INTEGER NOT NULL,
                    unit_price DECIMAL(12,2),
                    total_price DECIMAL(12,2),
                    note TEXT,
                    user_id INTEGER REFERENCES users(id),
                    supplier TEXT,
                    customer TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Indexes
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_product ON transactions(product_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(created_at)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id)");

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации схемы БД: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}
