package ru.kata.spring.boot_security.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;

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
}
