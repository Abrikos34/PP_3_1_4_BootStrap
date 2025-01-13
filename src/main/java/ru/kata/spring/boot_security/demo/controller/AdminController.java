package ru.kata.spring.boot_security.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String showAdminPage(Model model) {
        model.addAttribute("message", "Welcome to the Admin Page!");
        model.addAttribute("name", "Admin User");
        return "admin";
    }

    @GetMapping("/profile")
    public String showAdminProfile(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "User not authenticated.");
            return "error";
        }
        try {
            User admin = userService.getUserByUsername(principal.getName());
            model.addAttribute("admin", admin);
            return "adminProfile";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении данных профиля.");
            return "error";
        }
    }

    @GetMapping("/users")
    public String showUserList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "list";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "create";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user, @RequestParam("roles") List<Long> roleIds, Model model) {
        return handleUserSaveOrUpdate(() -> userService.saveUserWithRoles(user, roleIds), model, "create");
    }

    @GetMapping("/users/edit")
    public String showEditUserForm(@RequestParam Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", roleService.getAllRoles());
        return "edit";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user, @RequestParam("roles") List<Long> roleIds, Model model) {
        return handleUserSaveOrUpdate(() -> userService.saveUserWithRoles(user, roleIds), model, "edit");
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam("id") Long id, Model model) {
        try {
            userService.deleteUser(id);
        } catch (RuntimeException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private String handleUserSaveOrUpdate(Runnable saveOrUpdateAction, Model model, String viewName) {
        try {
            saveOrUpdateAction.run();
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            logger.error("Error saving or updating user: {}", e.getMessage());
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            model.addAttribute("roles", roleService.getAllRoles());
            return viewName;
        }
    }
}
