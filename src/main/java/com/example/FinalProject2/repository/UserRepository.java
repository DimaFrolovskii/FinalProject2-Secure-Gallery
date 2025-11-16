package com.example.FinalProject2.repository;

import com.example.FinalProject2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Метод, который Spring Data JPA реализует автоматически
    // для поиска пользователя по имени (это нужно для Spring Security)
    Optional<User> findByUsername(String username);
}