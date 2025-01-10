package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String showAdminPage(Model model) {
        // Передаем необходимые атрибуты в модель
        model.addAttribute("message", "Welcome to the Admin Page!");
        model.addAttribute("name", "Admin User"); // Добавьте 'name', если он используется в шаблоне
        return "admin"; // Убедитесь, что шаблон admin.html существует
    }
}
