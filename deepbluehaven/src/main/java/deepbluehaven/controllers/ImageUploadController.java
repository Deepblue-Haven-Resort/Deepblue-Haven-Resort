package deepbluehaven.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import deepbluehaven.services.CloudinaryStorageService;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);

    private final CloudinaryStorageService cloudinaryStorageService;

    public ImageUploadController(CloudinaryStorageService cloudinaryStorageService) {
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "deepbluehaven/uploads") String folder) {
        
        log.info("[ImageUploadController] Received upload request. File: {}, Folder: {}", 
                file != null ? file.getOriginalFilename() : "null", folder);

        Map<String, Object> response = new HashMap<>();

        if (file == null || file.isEmpty()) {
            log.warn("[ImageUploadController] Rejected empty file request");
            response.put("success", false);
            response.put("message", "Selected file is empty.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String uploadedUrl = cloudinaryStorageService.uploadImage(file, folder);
            log.info("[ImageUploadController] Upload successful. Final image URL: {}", uploadedUrl);
            response.put("success", true);
            response.put("url", uploadedUrl);
            response.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[ImageUploadController] Upload request failed with exception", e);
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
