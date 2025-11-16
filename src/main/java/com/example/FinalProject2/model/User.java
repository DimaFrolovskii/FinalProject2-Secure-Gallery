package com.example.FinalProject2.model; // УДАЛЕНА ЛИШНЯЯ ТОЧКА С ЗАПЯТОЙ (;)

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
// Импорт для Image, если он в том же пакете:
// import com.example.FinalProject2.model.Image;
// Если Image находится в другом пакете, замените на правильный путь.

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role = "USER";

    // Связь с таблицей Image
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images;

    // --- КОНСТРУКТОРЫ ---
    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ ---
    // (Подразумевается, что они у вас уже есть, за исключением методов UserDetails)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ГЕТТЕР И СЕТТЕР ДЛЯ КОЛЛЕКЦИИ ИЗОБРАЖЕНИЙ
    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    // --- РЕАЛИЗАЦИЯ МЕТОДОВ ИНТЕРФЕЙСА UserDetails (ИСПРАВЛЕНО) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ЭТО ИСПРАВЛЯЕТ ОШИБКУ: 'Class 'User' must either be declared abstract or implement abstract method 'getAuthorities()''
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password; // Возвращает поле 'password'
    }

    @Override
    public String getUsername() {
        return username; // Возвращает поле 'username'
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}