package com.mealgpt.service;

import com.mealgpt.config.MealGptProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {
    private final Path uploadDir;

    public UploadService(MealGptProperties properties) throws IOException {
        this.uploadDir = Paths.get(properties.getUploadDir());
        Files.createDirectories(uploadDir);
    }

    public Path saveUploadFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String filename = Paths.get(originalName).getFileName().toString();
        Path target = uploadDir.resolve(filename).normalize();
        file.transferTo(target);
        return target;
    }
}
