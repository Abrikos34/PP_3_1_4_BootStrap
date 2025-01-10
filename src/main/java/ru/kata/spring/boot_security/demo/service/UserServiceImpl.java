package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll(); // Получение всех пользователей
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        if (user.getId() != null) {
            // Если это обновление пользователя
            User existingUser = userDao.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь с ID " + user.getId() + " не найден"));

            // Если пароль пустой, оставляем старый
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                // Хэшируем новый пароль
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            // Если это новый пользователь, хэшируем пароль
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new RuntimeException("Пароль не может быть пустым для нового пользователя");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Проверяем дубликаты имени пользователя
        userDao.findByUsername(user.getUsername()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Ошибка: Пользователь с таким именем уже существует!");
            }
        });

        try {
            userDao.save(user); // Сохранение пользователя
            System.out.println("✅ Пользователь успешно сохранен: " + user.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении пользователя: " + user.getUsername(), e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null.");
        }
        try {
            userDao.deleteById(id); // Удаление пользователя
            System.out.println("✅ Пользователь с ID " + id + " успешно удалён.");
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Некорректный ID пользователя: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при удалении пользователя: " + e.getMessage());
            throw new RuntimeException("Ошибка при удалении пользователя с ID: " + id, e);
        }
    }

    @Override
    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с именем " + username + " не найден"));
    }
}
