package com.example.FinalProject2; // Убедитесь, что пакет правильный (если вы его используете)

import com.example.FinalProject2.model.Image;
import com.example.FinalProject2.model.User;
import com.example.FinalProject2.repository.ImageRepository;
import com.example.FinalProject2.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Импорты для работы с файлами
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploaded-images";
    private final Path rootLocation = Paths.get(UPLOAD_DIR);

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    // Приватный класс для передачи данных об изображении и его авторе в шаблон
    private static class ImageInfo {
        private final String filename;
        private final String uploaderName;

        public ImageInfo(String filename, String uploaderName) {
            this.filename = filename;
            this.uploaderName = uploaderName;
        }

        public String getFilename() {
            return filename;
        }

        public String getUploaderName() {
            return uploaderName;
        }
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать папку для загрузок!", e);
        }
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }

    // ----------------------------------------------------------------------
    // 5. GET /hello (ПОКАЗАТЬ ВСЕ КАРТИНКИ И ИМЯ АВТОРА)
    // ----------------------------------------------------------------------
    @GetMapping("/hello")
    public String listUploadedFiles(Model model) {
        try {
            List<Image> allImages = imageRepository.findAll();

            // Создаем список DTO ImageInfo
            List<ImageInfo> imagesInfo = allImages.stream()
                    .map(image -> new ImageInfo(
                            image.getFilename(),
                            image.getUser().getUsername() // Получаем имя пользователя
                    ))
                    .collect(Collectors.toList());

            // Передаем новый список в модель
            model.addAttribute("imagesInfo", imagesInfo);

        } catch (Exception e) {
            model.addAttribute("message", "Ошибка при загрузке списка файлов: " + e.getMessage());
        }
        return "hello";
    }

    // ----------------------------------------------------------------------
    // 6. POST /upload (ЛОГИКА ПЕРЕИМЕНОВАНИЯ ДУБЛИКАТОВ)
    // ----------------------------------------------------------------------
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Пожалуйста, выберите файл для загрузки.");
            return "redirect:/hello";
        }

        // Объявляем filename перед try, чтобы он был доступен в catch
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = null;

        try {
            // 1. ПРОВЕРКА И ПЕРЕИМЕНОВАНИЕ ДУБЛИКАТОВ
            uniqueFilename = generateUniqueFilename(originalFilename);

            // 2. Сохраняем файл на диск (теперь используем uniqueFilename)
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile);

            // 3. Сохраняем МЕТАДАННЫЕ в БД
            User currentUser = getCurrentUser(authentication);
            Image newImage = new Image(uniqueFilename, currentUser);
            imageRepository.save(newImage);

            redirectAttributes.addFlashAttribute("message",
                    "Вы успешно загрузили '" + uniqueFilename + "'!");
        } catch (IOException e) {
            String nameToShow = (uniqueFilename != null) ? uniqueFilename : originalFilename;
            redirectAttributes.addFlashAttribute("message",
                    "Не удалось загрузить '" + nameToShow + "'. Ошибка: " + e.getMessage());
        }
        return "redirect:/hello";
    }

    // 7. POST /delete (Удаление) - Логика не меняется
    @PostMapping("/delete")
    public String deleteFile(@RequestParam("filename") String filename,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {

        try {
            User currentUser = getCurrentUser(authentication);

            Optional<Image> imageOpt = imageRepository.findByFilenameAndUser(filename, currentUser);

            if (imageOpt.isPresent()) {
                Path fileToDelete = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
                Files.deleteIfExists(fileToDelete);

                imageRepository.delete(imageOpt.get());

                redirectAttributes.addFlashAttribute("message", "Файл '" + filename + "' успешно удален.");
            } else {
                redirectAttributes.addFlashAttribute("message", "Ошибка: Файл не найден или у вас нет прав на его удаление.");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении файла '" + filename + "': " + e.getMessage());
        }

        return "redirect:/hello";
    }

    // 8. GET /images/{filename} (Отображение) - Остается без изменений
    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
            } else {
                throw new RuntimeException("Не удалось прочитать файл: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + filename, e);
        }
    }

    // ----------------------------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД: Генерация уникального имени (ИСПРАВЛЕНО)
    // ----------------------------------------------------------------------
    private String generateUniqueFilename(String originalFilename) {
        String baseName = originalFilename;
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        }

        int counter = 0;
        String currentFilename = originalFilename;

        // ИСПРАВЛЕННАЯ ЛОГИКА:
        while (true) {
            // СОЗДАЕМ ЛОКАЛЬНУЮ ФИНАЛЬНУЮ КОПИЮ для лямбда-выражения
            final String comparisonFilename = currentFilename;

            // Проверяем, существует ли файл с текущим именем
            boolean exists = imageRepository.findByFilenameStartingWith(comparisonFilename).stream()
                    // Сравниваем точно, так как findByFilenameStartingWith может вернуть файлы типа "file(1).jpg"
                    .anyMatch(img -> img.getFilename().equals(comparisonFilename));

            if (!exists) {
                break; // Имя уникально, выходим из цикла
            }

            // Если имя существует, генерируем следующее имя для проверки
            counter++;
            currentFilename = baseName + "(" + counter + ")" + extension;
        }

        return currentFilename;
    }
}