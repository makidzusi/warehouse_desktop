package com.warehouse.repository;

import com.warehouse.model.Category;
import com.warehouse.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения категорий: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска категории: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Category save(Category category) {
        if (category.getId() == 0) return insert(category);
        else return update(category);
    }

    private Category insert(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) category.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания категории: " + e.getMessage(), e);
        }
        return category;
    }

    private Category update(Category category) {
        String sql = "UPDATE categories SET name=?, description=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления категории: " + e.getMessage(), e);
        }
        return category;
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM categories WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления категории: " + e.getMessage(), e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}
