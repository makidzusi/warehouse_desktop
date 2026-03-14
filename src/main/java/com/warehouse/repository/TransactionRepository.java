package com.warehouse.repository;

import com.warehouse.model.Transaction;
import com.warehouse.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    private static final String SELECT_BASE = """
        SELECT t.*, p.name AS product_name, p.sku AS product_sku,
               u.full_name AS user_name
        FROM transactions t
        LEFT JOIN products p ON t.product_id = p.id
        LEFT JOIN users u ON t.user_id = u.id
    """;

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY t.created_at DESC LIMIT 500";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения транзакций: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findByProductId(int productId) {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.product_id = ? ORDER BY t.created_at DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения транзакций по товару: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE t.created_at BETWEEN ? AND ? ORDER BY t.created_at DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации транзакций: " + e.getMessage(), e);
        }
        return list;
    }

    public Transaction save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions
            (type, product_id, quantity, unit_price, total_price, note, user_id, supplier, customer)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, transaction.getType().name());
            ps.setInt(2, transaction.getProductId());
            ps.setInt(3, transaction.getQuantity());
            ps.setBigDecimal(4, transaction.getUnitPrice());
            ps.setBigDecimal(5, transaction.getTotalPrice());
            ps.setString(6, transaction.getNote());
            ps.setInt(7, transaction.getUserId());
            ps.setString(8, transaction.getSupplier());
            ps.setString(9, transaction.getCustomer());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) transaction.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания транзакции: " + e.getMessage(), e);
        }
        return transaction;
    }

    // Analytics: total incoming per last N days grouped by date
    public Map<String, Double> getDailyIncoming(int days) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = """
            SELECT DATE(created_at) AS day, SUM(total_price) AS total
            FROM transactions
            WHERE type = 'INCOMING' AND created_at >= DATE('now', ?)
            GROUP BY day ORDER BY day
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, "-" + days + " days");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("day"), rs.getDouble("total"));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка аналитики прихода: " + e.getMessage(), e);
        }
        return result;
    }

    public Map<String, Double> getDailyOutgoing(int days) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = """
            SELECT DATE(created_at) AS day, SUM(total_price) AS total
            FROM transactions
            WHERE type = 'OUTGOING' AND created_at >= DATE('now', ?)
            GROUP BY day ORDER BY day
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, "-" + days + " days");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("day"), rs.getDouble("total"));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка аналитики расхода: " + e.getMessage(), e);
        }
        return result;
    }

    public Map<String, Integer> getTopProductsByMovement(int limit) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = """
            SELECT p.name, SUM(t.quantity) AS total_qty
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.type IN ('INCOMING','OUTGOING')
            GROUP BY t.product_id ORDER BY total_qty DESC LIMIT ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("name"), rs.getInt("total_qty"));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка аналитики топ-товаров: " + e.getMessage(), e);
        }
        return result;
    }

    public BigDecimal getTotalValueIncoming() {
        return getSumByType("INCOMING");
    }

    public BigDecimal getTotalValueOutgoing() {
        return getSumByType("OUTGOING");
    }

    private BigDecimal getSumByType(String type) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM transactions WHERE type = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка вычисления суммы: " + e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    public long countToday() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE DATE(created_at) = DATE('now')";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setType(Transaction.Type.valueOf(rs.getString("type")));
        t.setProductId(rs.getInt("product_id"));
        t.setProductName(rs.getString("product_name"));
        t.setProductSku(rs.getString("product_sku"));
        t.setQuantity(rs.getInt("quantity"));
        t.setUnitPrice(rs.getBigDecimal("unit_price"));
        t.setTotalPrice(rs.getBigDecimal("total_price"));
        t.setNote(rs.getString("note"));
        t.setUserId(rs.getInt("user_id"));
        t.setUserName(rs.getString("user_name"));
        t.setSupplier(rs.getString("supplier"));
        t.setCustomer(rs.getString("customer"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            t.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        return t;
    }
}
