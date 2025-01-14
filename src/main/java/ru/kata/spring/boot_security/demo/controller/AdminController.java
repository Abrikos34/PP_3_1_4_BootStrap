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


    @GetMapping("/users")
    public String showUserList(Model model, Principal principal) {
        addCurrentUserToModel(model, principal);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("page", "admin");
        return "list";
    }


    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            model.addAttribute("error", "User not authenticated.");
            return "error";
        }
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            addCurrentUserToModel(model, principal);

            model.addAttribute("user", currentUser);
            model.addAttribute("page", "user");
            return "list";
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при получении данных профиля.");
            return "error";
        }
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam("roles") List<Long> roleIds,
                           Model model, Principal principal) {
        addCurrentUserToModel(model, principal);
        return handleUserSaveOrUpdate(() -> userService.saveUserWithRoles(user, roleIds),
                model, "list");
    }

    @GetMapping("/users/edit")
    public String showEditUserForm(@RequestParam Long id, Model model, Principal principal) {
        addCurrentUserToModel(model, principal);
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.getAllRoles());
            return "edit";
        } catch (RuntimeException e) {
            logger.error("Error loading user for edit: {}", e.getMessage());
            model.addAttribute("error", "User not found: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam("roles") List<Long> roleIds,
                             Model model, Principal principal) {
        addCurrentUserToModel(model, principal);
        return handleUserSaveOrUpdate(() -> userService.saveUserWithRoles(user, roleIds),
                model, "edit");
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam("id") Long id, Model model, Principal principal) {
        addCurrentUserToModel(model, principal);
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

    private void addCurrentUserToModel(Model model, Principal principal) {
        if (principal != null && principal.getName() != null) {
            try {
                User currentUser = userService.getUserByEmail(principal.getName());
                model.addAttribute("currentUser", currentUser);
            } catch (Exception e) {
                logger.error("Error retrieving current user: {}", e.getMessage());
            }
        }
    }
}
