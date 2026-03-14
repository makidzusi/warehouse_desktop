package com.warehouse.repository;

import com.warehouse.model.User;
import com.warehouse.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND active = 1";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения списка пользователей: " + e.getMessage(), e);
        }
        return users;
    }

    public User save(User user) {
        if (user.getId() == 0) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания пользователя: " + e.getMessage(), e);
        }
        return user;
    }

    private User update(User user) {
        String sql = "UPDATE users SET username=?, password_hash=?, full_name=?, role=?, active=? WHERE id=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setInt(6, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления пользователя: " + e.getMessage(), e);
        }
        return user;
    }

    public boolean existsAny() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setActive(rs.getInt("active") == 1);
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        return user;
    }
}
