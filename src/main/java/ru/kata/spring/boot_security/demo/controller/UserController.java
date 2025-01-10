package ru.kata.spring.boot_security.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.dao.RoleRepository;
import java.security.Principal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users") // Маршруты теперь доступны только через /admin/users
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    // Показать список пользователей
    @GetMapping
    public String showUserList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "list";
    }

    // Показать форму для создания нового пользователя
    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll()); // Передача списка ролей
        return "create";
    }

    // Сохранить нового пользователя
    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, @RequestParam("roles") List<Long> roleIds, Model model) {
        try {
            // Преобразуем список ID ролей в Set<Role>
            Set<Role> roles = roleIds.stream()
                    .map(roleRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(roles);

            // Сохраняем пользователя
            userService.saveUser(user);
        } catch (RuntimeException e) {
            // Логируем ошибку
            logger.error("Error saving user: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при сохранении пользователя: " + e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "create";
        }
        return "redirect:/admin/users";
    }


    // Показать форму редактирования пользователя
    @GetMapping("/edit")
    public String showEditUserForm(@RequestParam Long id, Model model) {
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll()); // Передача списка ролей для редактирования
            return "edit";
        } catch (RuntimeException e) {
            logger.warn("User with ID {} not found: {}", id, e.getMessage());
            model.addAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
    }

    // Обновить пользователя
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, @RequestParam("roles") List<Long> roleIds, Model model) {
        try {
            // Проверяем, если пароль пустой, то берем текущий из базы данных
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                User existingUser = userService.getUserById(user.getId());
                user.setPassword(existingUser.getPassword());
            }

            // Обрабатываем роли
            Set<Role> roles = roleIds.stream()
                    .map(roleRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(roles);

            // Сохраняем пользователя
            userService.saveUser(user);
        } catch (RuntimeException e) {
            // Логируем и передаем ошибку в модель
            logger.error("Error updating user: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "edit"; // Возвращаемся на форму редактирования
        }
        return "redirect:/admin/users"; // Редирект после успешного обновления
    }


    // Удалить пользователя
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id, Model model) {
        try {
            userService.deleteUser(id);
        } catch (RuntimeException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
