package ru.kata.spring.boot_security.demo.configs;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

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
                .csrf(csrf -> csrf.disable()) // Отключение CSRF для REST API
                .authorizeHttpRequests(auth -> auth
                        // Доступ к REST-контроллерам
                        .requestMatchers("/api/users/profile/**").hasAnyRole("USER", "ADMIN") // Доступ к своему профилю
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Управление пользователями только для админов
                        .requestMatchers("/api/roles/**").hasRole("ADMIN") // REST API для ролей

                        // Доступ к обычным интерфейсам
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Админский интерфейс
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // Интерфейс для пользователей

                        // Публичные маршруты
                        .requestMatchers("/login", "/error", "/").permitAll() // Доступ к логину и главной странице
                        .anyRequest().authenticated() // Остальные маршруты требуют авторизации
                )
                .httpBasic(basic -> basic // Basic Auth для REST API
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login") // Страница входа
                        .loginProcessingUrl("/process_login") // Обработчик логина
                        .usernameParameter("email") // Поле для логина
                        .passwordParameter("password") // Поле для пароля
                        .successHandler(successUserHandler) // Обработчик успешного входа
                        .failureUrl("/login?error") // URL при ошибке входа
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL для выхода
                        .logoutSuccessUrl("/login?logout") // Куда перенаправить после выхода
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Шифрование паролей
    }
}
