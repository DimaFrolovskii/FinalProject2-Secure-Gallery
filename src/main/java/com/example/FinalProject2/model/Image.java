package com.example.FinalProject2.model; // Убедитесь, что пакет правильный

import jakarta.persistence.*;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename; // Имя файла (часть URL)

    // Связь: Многие изображения могут принадлежать одному пользователю
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- Конструкторы, Геттеры и Сеттеры ---
    public Image() {
    }

    public Image(String filename, User user) {
        this.filename = filename;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}