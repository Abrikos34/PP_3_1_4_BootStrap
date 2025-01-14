package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.UserRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userDao;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userDao, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        validateUniqueEmail(user);
        processUserPassword(user);
        userDao.save(user);
    }

    @Override
    @Transactional
    public void saveUserWithRoles(User user, List<Long> roleIds) {
        Set<Role> roles = roleService.getRolesByIds(roleIds);
        user.setRoles(roles);
        validateUniqueEmail(user);
        processUserPassword(user);
        userDao.save(user);
    }

    private void processUserPassword(User user) {
        if (user.getId() != null) {
            User existingUser = userDao.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь с ID " + user.getId() + " не найден"));
            user.setPassword(getUpdatedPassword(user, existingUser));
        } else {
            validateNewUserPassword(user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    private String getUpdatedPassword(User user, User existingUser) {
        return (user.getPassword() == null || user.getPassword().isEmpty())
                ? existingUser.getPassword()
                : passwordEncoder.encode(user.getPassword());
    }

    private void validateNewUserPassword(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Пароль не может быть пустым для нового пользователя");
        }
    }

    private void validateUniqueEmail(User user) {
        userDao.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Ошибка: Пользователь с таким email уже существует!");
            }
        });
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null.");
        }
        userDao.deleteById(id);
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

}
