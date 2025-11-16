package com.example.FinalProject2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize

                        // РАЗРЕШАЕМ ПУБЛИЧНЫЕ СТРАНИЦЫ
                        .requestMatchers("/", "/home", "/index").permitAll()
                        .requestMatchers("/login", "/register").permitAll()

                        // РАЗРЕШАЕМ ПРОСМОТР ЗАГРУЖЕННЫХ ИЗОБРАЖЕНИЙ
                        .requestMatchers("/images/**").permitAll()

                        // ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ (включая /hello) ТРЕБУЮТ АУТЕНТИФИКАЦИИ
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // ПЕРЕНАПРАВЛЕНИЕ НА СТРАНИЦУ ЗАГРУЗКИ ФАЙЛОВ (/hello)
                        .defaultSuccessUrl("/hello", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }
}