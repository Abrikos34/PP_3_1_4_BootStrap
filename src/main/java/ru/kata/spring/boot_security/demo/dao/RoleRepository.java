package ru.kata.spring.boot_security.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kata.spring.boot_security.demo.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Возвращаем Optional для обработки возможного отсутствия роли
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);
}
