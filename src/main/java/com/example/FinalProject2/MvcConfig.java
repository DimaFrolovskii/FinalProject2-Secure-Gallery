package com.example.FinalProject2; // Убедитесь, что пакет правильный

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // Импорт для изображений
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File; // Импорт для File.separator

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Путь к папке с загруженными изображениями (из вашего FileUploadController)
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploaded-images";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // 1. СТАРТ: / и /home показывают 'home.html'
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/home").setViewName("home");

        // 2. ВЫБОР: /index показывает 'index.html'
        registry.addViewController("/index").setViewName("index");

        // 3. АУТЕНТИФИКАЦИЯ: /login и /register
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/register").setViewName("registration");

        // 4. ЗАЩИЩЕННАЯ СТРАНИЦА: /hello показывает 'hello.html' (загрузка файлов)
        registry.addViewController("/hello").setViewName("hello");
    }

    // 5. ВОЗВРАЩАЕМ ОБРАБОТЧИК РЕСУРСОВ (для загруженных изображений)
    // Этот код необходим, чтобы ваши загруженные фото отображались на /hello
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Указываем, что запросы по /images/** должны обслуживаться из вашей папки загрузок
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + UPLOAD_DIR + File.separator);
    }
}