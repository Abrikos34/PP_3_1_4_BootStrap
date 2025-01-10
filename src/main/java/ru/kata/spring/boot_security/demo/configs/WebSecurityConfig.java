package ru.kata.spring.boot_security.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final SuccessUserHandler successUserHandler;

    public WebSecurityConfig(SuccessUserHandler successUserHandler) {
        this.successUserHandler = successUserHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для упрощения
                .csrf(csrf -> csrf.disable())
                // Настройка авторизационных правил
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Только для ROLE_ADMIN
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // ROLE_USER и ROLE_ADMIN
                        .requestMatchers("/login", "/error", "/").permitAll() // Доступ для всех
                        .anyRequest().authenticated() // Все остальные запросы требуют авторизации
                )
                // Настройка логина
                .formLogin(form -> form
                        .loginPage("/login") // Указываем кастомную страницу логина
                        .loginProcessingUrl("/process_login") // URL для обработки логина
                        .successHandler(successUserHandler) // Используем SuccessUserHandler
                        .failureUrl("/login?error") // Перенаправление при ошибке
                        .permitAll()
                )
                // Настройка логаута
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL для логаута
                        .logoutSuccessUrl("/login?logout") // Перенаправление после выхода
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Используем BCrypt для шифрования паролей
    }
}
