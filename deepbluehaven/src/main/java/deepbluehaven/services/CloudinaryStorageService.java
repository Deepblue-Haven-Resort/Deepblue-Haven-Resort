package deepbluehaven.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

@Service
public class CloudinaryStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            log.warn("[ImageUpload] Attempted to upload empty file");
            throw new IllegalArgumentException("Image file is empty");
        }

        String filename = file.getOriginalFilename();
        long size = file.getSize();
        log.info("[ImageUpload] Starting upload for file: {}, size: {} bytes, target folder: {}", filename, size, folder);

        try {
            String publicId = UUID.randomUUID().toString();
            String targetFolder = (folder == null || folder.isBlank()) ? "deepbluehaven/uploads" : folder;

            @SuppressWarnings("unchecked")
            Map<String, Object> res = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of(
                    "folder", targetFolder,
                    "public_id", publicId,
                    "resource_type", "image"
                )
            );

            Object secureUrl = res.get("secure_url");
            if (secureUrl != null) {
                String urlStr = secureUrl.toString();
                log.info("[ImageUpload] Successfully uploaded to Cloudinary: {}", urlStr);
                return urlStr;
            } else {
                log.warn("[ImageUpload] Cloudinary response missing secure_url: {}", res);
            }
        } catch (Exception e) {
            log.error("[ImageUpload] Cloudinary API upload failed! Cause: {}", e.getMessage(), e);
        }

        return saveFileLocally(file);
    }

    private String saveFileLocally(MultipartFile file) {
        try {
            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            } else {
                extension = ".png";
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path uploadDir = Paths.get("target/classes/static/uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path destPath = uploadDir.resolve(filename);
            file.transferTo(destPath.toFile());

            String localUrl = "/deepbluehaven/uploads/" + filename;
            log.info("[ImageUpload] Fallback image saved locally at: {}", localUrl);
            return localUrl;
        } catch (IOException ioException) {
            log.error("[ImageUpload] Local file fallback storage also failed!", ioException);
            throw new RuntimeException("Failed to save image file locally: " + ioException.getMessage(), ioException);
        }
    }
}
