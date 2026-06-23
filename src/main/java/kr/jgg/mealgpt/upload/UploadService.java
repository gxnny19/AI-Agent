package kr.jgg.mealgpt.upload;

import kr.jgg.mealgpt.config.MealGptProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UploadService {
    private final MealGptProperties properties;
    private Path uploadDir;

    public UploadService(MealGptProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    public Path save(MultipartFile file) throws IOException {
        String filename = Paths.get(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename())
                .getFileName()
                .toString();
        Path target = uploadDir.resolve(filename);
        Files.createDirectories(uploadDir);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    public Path resolveUploadedFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return latestUpload()
                    .orElseThrow(() -> new IllegalArgumentException("업로드된 파일이 없습니다."));
        }
        Path resolved = uploadDir.resolve(Paths.get(filename).getFileName().toString()).normalize();
        if (!resolved.startsWith(uploadDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        if (!Files.exists(resolved)) {
            throw new IllegalArgumentException("업로드 파일을 찾을 수 없습니다: " + filename);
        }
        return resolved;
    }

    public Optional<Path> latestUpload() {
        try (Stream<Path> files = Files.list(uploadDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(this::lastModified));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            return 0L;
        }
    }
}
