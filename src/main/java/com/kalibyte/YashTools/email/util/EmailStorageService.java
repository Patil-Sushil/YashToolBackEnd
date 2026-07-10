package com.kalibyte.YashTools.email.util;

import com.kalibyte.YashTools.email.config.EmailProperties;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailStorageService {

    private final EmailProperties emailProperties;

    public String persist(UUID emailLogId, EmailAttachment attachment) {
        try {
            Path dir = Paths.get(emailProperties.getStoragePath(), emailLogId.toString());
            Files.createDirectories(dir);
            String safeName = (attachment.getFilename() == null ? "file" : attachment.getFilename())
                    .replaceAll("[^A-Za-z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safeName);
            byte[] bytes = attachment.getData() != null
                    ? attachment.getData()
                    : (attachment.getInputStream() != null
                       ? attachment.getInputStream().readAllBytes()
                       : null);
            if (bytes == null) throw new EmailException("Attachment has no data: " + attachment.getFilename());
            Files.write(target, bytes);
            return target.toString();
        } catch (IOException e) {
            throw new EmailException("Failed to persist attachment", e);
        }
    }

    public byte[] read(String storagePath) {
        try {
            return Files.readAllBytes(Paths.get(storagePath));
        } catch (IOException e) {
            throw new EmailException("Failed to read attachment: " + storagePath, e);
        }
    }
}