package com.kompu.api.infrastructure.shared.gateway;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kompu.api.entity.shared.gateway.FileStorageGateway;

import lombok.extern.slf4j.Slf4j;

/**
 * Local file system implementation of FileStorageGateway.
 */
@Slf4j
@Component
public class LocalFileStorageGateway implements FileStorageGateway {

    private final String uploadDir;

    // Regex to detect and strip Data URI scheme: data:image/png;base64,....
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:(.*?);base64,(.*)$", Pattern.DOTALL);

    public LocalFileStorageGateway(@Value("${app.storage.local.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public String saveBase64File(String originalFilename, String base64Content, String subDirectory) {
        if (base64Content == null || base64Content.isEmpty()) {
            return null;
        }

        try {
            String encodedString = base64Content;
            String extension = "";

            // Check for Data URI scheme and extract mime type / extension
            Matcher matcher = DATA_URI_PATTERN.matcher(base64Content);
            if (matcher.matches()) {
                String mimeType = matcher.group(1);
                encodedString = matcher.group(2);
                extension = getExtensionFromMimeType(mimeType);
            }

            // Decode
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);

            // Prepare directory
            Path directoryPath = Paths.get(uploadDir, subDirectory);
            Files.createDirectories(directoryPath);

            // Prepare filename
            String filename = originalFilename;
            if (extension != null && !extension.isEmpty() && !filename.endsWith("." + extension)) {
                filename += "." + extension;
            } else if (!filename.contains(".")) {
                // If no extension found and no extension in name, validation might fail later
                // or file
                // type defaults to bin, but let's try to proceed.
            }

            // Avoid collisions if necessary, or just overwrite for profile photo?
            // "profile_photo" + unique? For now executing basic requirement.

            Path filePath = directoryPath.resolve(filename);

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(decodedBytes);
            }

            // Return relative path. Consumers (controllers) can prepend host/base URL.
            return Paths.get(subDirectory, filename).toString();

        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 input", e);
            throw new RuntimeException("Failed to decode Base64 content", e);
        } catch (IOException e) {
            log.error("Failed to write file to disk", e);
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private String getExtensionFromMimeType(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            case "application/pdf":
                return "pdf";
            default:
                return ""; // Unknown or no extension inferred
        }
    }
}
