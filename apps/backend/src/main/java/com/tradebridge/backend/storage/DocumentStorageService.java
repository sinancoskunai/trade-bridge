package com.tradebridge.backend.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

    private final Path baseDir;

    public DocumentStorageService(@Value("${app.storage.local-dir:/tmp/tradebridge/uploads}") String localDir) {
        this.baseDir = Path.of(localDir);
    }

    public StoredFile store(String draftId, MultipartFile file) throws IOException {
        Files.createDirectories(baseDir);

        String original = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = draftId + "-" + UUID.randomUUID() + "-" + safeName;
        Path target = baseDir.resolve(objectName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        return new StoredFile(target.toString(), file.getSize(), contentType);
    }
}
