package com.warehouse.service;

import com.warehouse.model.User;
import com.warehouse.repository.UserRepository;
import com.warehouse.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionManager session = SessionManager.getInstance();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) return false;
        session.login(user);
        return true;
    }

    public void logout() {
        session.logout();
    }

    public User register(String username, String password, String fullName, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hash, fullName, role);
        return userRepository.save(user);
    }

    public boolean hasAnyUser() {
        return userRepository.existsAny();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void changePassword(int userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            userRepository.save(user);
        });
    }

    public void createDefaultAdmin() {
        if (!userRepository.existsAny()) {
            register("admin", "admin123", "Администратор", User.Role.ADMIN);
        }
    }
}
