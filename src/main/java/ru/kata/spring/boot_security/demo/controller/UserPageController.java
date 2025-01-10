package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;

@Controller
public class UserPageController {

    private final UserService userService;

    @Autowired
    public UserPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String showUserPage(Principal principal, Model model) {
        // Проверяем, что пользователь аутентифицирован
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "Не удалось определить пользователя.");
            return "error"; // Показываем страницу ошибки
        }

        // Получаем текущего аутентифицированного пользователя
        try {
            User user = userService.getUserByUsername(principal.getName()); // Используем username вместо email
            model.addAttribute("user", user);
            return "user"; // Ссылаемся на шаблон user.html
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            return "error"; // Показываем страницу ошибки
        }
    }
}
