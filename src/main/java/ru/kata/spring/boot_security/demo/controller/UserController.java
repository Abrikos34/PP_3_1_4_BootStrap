package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User addCurrentUserToModel(Principal principal) {
        if (principal != null && principal.getName() != null) {
            return userService.getUserByEmail(principal.getName());
        }
        return null;
    }

    @GetMapping
    public String showUserPage(Principal principal, Model model) {
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "Не удалось определить пользователя.");
            return "error";
        }
        // Используем поиск по email
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        return "user";
    }
}
