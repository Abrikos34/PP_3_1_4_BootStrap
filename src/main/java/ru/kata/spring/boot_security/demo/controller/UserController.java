package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showUserPage(Principal principal, Model model) {
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "Не удалось определить пользователя.");
            return "error";
        }
        User user = userService.getUserByUsername(principal.getName());
        model.addAttribute("user", user);
        return "user";
    }
}
