package nvt.vn.ute_forum.service.pattern.chain_of_responsibility;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Concrete Handler 2 — Upload/Delete trên disk server (fallback).
 */
public class LocalUploadHandler extends FileUploadHandler {

    private static final String URL_PREFIX = "/uploads/";

    private final String uploadDir;

    public LocalUploadHandler(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    // =========================================================
    // UPLOAD
    // =========================================================
    @Override
    protected String doUpload(MultipartFile file, String ownerId) throws Exception {
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);

        String originalName = file.getOriginalFilename() == null
                ? "file"
                : Paths.get(file.getOriginalFilename()).getFileName().toString();

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) extension = originalName.substring(dotIndex);

        String storedFileName = ownerId + "_" + System.nanoTime() + extension;
        Path target = uploadRoot.resolve(storedFileName).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new Exception("Đường dẫn file không hợp lệ.");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return URL_PREFIX + storedFileName;
    }

    // =========================================================
    // DELETE
    // =========================================================

    /** Nhận ra URL Local qua prefix "/uploads/" */
    @Override
    protected boolean canHandle(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith(URL_PREFIX);
    }

    @Override
    protected void doDelete(String fileUrl) {
        try {
            String relativePath = fileUrl.substring(URL_PREFIX.length());
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = uploadRoot.resolve(relativePath).normalize();

            if (!target.startsWith(uploadRoot)) return;

            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            System.err.println("[LocalUploadHandler] Xóa file thất bại: " + fileUrl);
        }
    }

    @Override
    protected void logFailure(Exception e) {
        System.err.println("[LocalUploadHandler] Local storage cũng thất bại: "
                + e.getMessage());
    }
}