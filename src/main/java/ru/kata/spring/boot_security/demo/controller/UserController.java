package ru.kata.spring.boot_security.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUser(Principal principal, @RequestBody User user) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = userService.getCurrentUser(principal.getName());
        if (!userService.canUpdateProfile(currentUser, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете обновить чужой профиль.");
        }
        userService.saveUser(user);
        return ResponseEntity.ok("Профиль успешно обновлён.");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Principal principal) {
        User currentUser = userService.getCurrentUser(principal.getName());
        if (!currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")) &&
                !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен.");
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            userService.saveUserWithRoles(user, user.getRoles()
                    .stream().map(Role::getId).collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Пользователь успешно создан!", "user", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь успешно обновлён!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @GetMapping("/login")
    public ResponseEntity<String> loginPage() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/login.html");
        String htmlContent = new String(Files.readAllBytes(resource.getFile().toPath()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=UTF-8");

        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }


}




