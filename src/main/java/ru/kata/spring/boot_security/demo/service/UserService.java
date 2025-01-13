package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    void saveUser(User user);
    void deleteUser(Long id);
    void saveUserWithRoles(User user, List<Long> roleIds);
    User getUserById(Long id);
    User getUserByEmail(String email); // Оставляем для совместимости
    User getUserByUsername(String username); // Новый метод
}
