package com.warehouse.repository;

import com.warehouse.model.Product;
import com.warehouse.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    private static final String SELECT_BASE = """
        SELECT p.*, c.name AS category_name
        FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
    """;

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY p.name";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения товаров: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Product> findLowStock() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE p.quantity <= p.min_quantity ORDER BY p.quantity";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения товаров с низким остатком: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Product> search(String query) {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE LOWER(p.name) LIKE ? OR LOWER(p.sku) LIKE ? ORDER BY p.name";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            String pattern = "%" + query.toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товаров: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Product> findById(int id) {
        String sql = SELECT_BASE + " WHERE p.id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товара: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Product save(Product product) {
        if (product.getId() == 0) return insert(product);
        else return update(product);
    }

    private Product insert(Product product) {
        String sql = "INSERT INTO products (name, sku, description, category_id, quantity, min_quantity, price, unit) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getDescription());
            if (product.getCategoryId() > 0) ps.setInt(4, product.getCategoryId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, product.getQuantity());
            ps.setInt(6, product.getMinQuantity());
            ps.setBigDecimal(7, product.getPrice());
            ps.setString(8, product.getUnit());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) product.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания товара: " + e.getMessage(), e);
        }
        return product;
    }

    private Product update(Product product) {
        String sql = "UPDATE products SET name=?, sku=?, description=?, category_id=?, quantity=?, min_quantity=?, price=?, unit=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getSku());
            ps.setString(3, product.getDescription());
            if (product.getCategoryId() > 0) ps.setInt(4, product.getCategoryId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, product.getQuantity());
            ps.setInt(6, product.getMinQuantity());
            ps.setBigDecimal(7, product.getPrice());
            ps.setString(8, product.getUnit());
            ps.setInt(9, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления товара: " + e.getMessage(), e);
        }
        return product;
    }

    public void updateQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления количества: " + e.getMessage(), e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления товара: " + e.getMessage(), e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= min_quantity";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setSku(rs.getString("sku"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinQuantity(rs.getInt("min_quantity"));
        BigDecimal price = rs.getBigDecimal("price");
        p.setPrice(price != null ? price : BigDecimal.ZERO);
        p.setUnit(rs.getString("unit"));
        return p;
    }
}
