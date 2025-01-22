package ru.kata.spring.boot_security.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public DataInitializer(UserService userService,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        if (userService.getUserByEmail("admin@example.com") == null) {
            User admin = new User(
                    "admin@example.com",
                    passwordEncoder.encode("admin"),
                    Set.of(adminRole)
            );
            userService.saveUser(admin);
        }

        if (userService.getUserByEmail("user@example.com") == null) {
            User user = new User(
                    "user@example.com",
                    passwordEncoder.encode("user"),
                    Set.of(userRole)
            );
            userService.saveUser(user);
        }

        System.out.println("Тестовые пользователи успешно добавлены.");
    }
}
