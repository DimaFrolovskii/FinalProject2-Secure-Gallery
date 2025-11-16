package com.example.FinalProject2.controller;

import com.example.FinalProject2.model.User;
import com.example.FinalProject2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Отображение формы регистрации (GET /register)
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Передаем в шаблон пустой объект User для заполнения полей формы
        model.addAttribute("user", new User());
        return "registration"; // Название HTML-шаблона
    }

    // 2. Обработка отправки формы (POST /register)
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {

        // --- Критический шаг: Шифрование пароля ---
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Устанавливаем роль по умолчанию (если она есть в вашей модели User)
        user.setRole("USER"); // Или другую роль по умолчанию

        // Сохраняем нового пользователя в базу данных
        userRepository.save(user);

        // Перенаправляем пользователя на страницу входа после успешной регистрации
        return "redirect:/login";
    }
}