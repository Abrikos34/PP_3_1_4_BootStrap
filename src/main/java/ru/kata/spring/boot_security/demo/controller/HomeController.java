package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/") // Перенаправление с корня
    public String redirectToHomePage() {
        return "redirect:/users"; // Или другой маршрут
    }

    @GetMapping("/login") // Добавляем обработчик для страницы входа
    public String loginPage() {
        return "login"; // Убедитесь, что login.html существует в папке templates
    }
}
