package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.model.User;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showAdminPage(Model model) {
        model.addAttribute("message", "Welcome to the Admin Page!");
        model.addAttribute("name", "Admin User");
        return "admin";
    }

    // Новый метод для отображения профиля
    @GetMapping("/profile")
    public String showAdminProfile(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "User not authenticated.");
            return "error"; // Перенаправление на страницу ошибки
        }

        try {
            // Получаем информацию о текущем пользователе (администраторе)
            User admin = userService.getUserByUsername(principal.getName());  // Используем getUserByUsername
            model.addAttribute("admin", admin); // Добавляем в модель
            return "adminProfile"; // Ссылаемся на новый шаблон для профиля
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении данных профиля.");
            return "error"; // Если ошибка, показываем страницу ошибки
        }
    }
}
