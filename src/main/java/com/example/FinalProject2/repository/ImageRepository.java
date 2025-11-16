package com.example.FinalProject2.repository; // Убедитесь, что пакет правильный

import com.example.FinalProject2.model.Image;
import com.example.FinalProject2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // Найти все изображения для конкретного пользователя (используется для удаления)
    Optional<Image> findByFilenameAndUser(String filename, User user);

    // Новый метод: Найти все файлы, начинающиеся с заданного префикса (для проверки дубликатов)
    List<Image> findByFilenameStartingWith(String filenamePrefix);

    // findByUser остается, хотя в текущей логике показа всех картинок он не используется,
    // он важен для других операций, например, удаления.
    List<Image> findByUser(User user);
}