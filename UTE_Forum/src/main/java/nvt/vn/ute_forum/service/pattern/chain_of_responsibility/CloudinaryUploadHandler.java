package nvt.vn.ute_forum.service.pattern.chain_of_responsibility;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Concrete Handler 1 — Upload/Delete trên Cloudinary CDN.
 */
public class CloudinaryUploadHandler extends FileUploadHandler {

    private static final String CLOUDINARY_URL_PREFIX = "https://res.cloudinary.com";

    private final Cloudinary cloudinary;

    public CloudinaryUploadHandler(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // =========================================================
    // UPLOAD
    // =========================================================
    @Override
    @SuppressWarnings("unchecked")
    protected String doUpload(MultipartFile file, String ownerId) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String resourceType = "auto";

        if (originalFilename != null) {
            String lowerName = originalFilename.toLowerCase();
            if (lowerName.endsWith(".pdf") || lowerName.endsWith(".doc")
                    || lowerName.endsWith(".docx") || lowerName.endsWith(".xlsx")) {
                resourceType = "raw"; // Bắt buộc là raw đối với tài liệu
            }
        }

        Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "folder",          "ute_forum/" + ownerId,
                "resource_type",   resourceType,
                "use_filename",    true,
                "unique_filename", true
        );

        Map<String, Object> result = cloudinary.uploader()
                .upload(file.getBytes(), uploadOptions);

        String secureUrl = (String) result.get("secure_url");
        if (secureUrl == null || secureUrl.isBlank()) {
            throw new Exception("Cloudinary không trả về URL hợp lệ.");
        }
        return secureUrl;
    }
    // =========================================================
    // DELETE
    // =========================================================

    /** Nhận ra URL Cloudinary qua prefix "https://res.cloudinary.com" */
    @Override
    protected boolean canHandle(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith(CLOUDINARY_URL_PREFIX);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doDelete(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId == null) return;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
            System.err.println("[CloudinaryUploadHandler] Xóa file thất bại: " + fileUrl);
        }
    }

    private String extractPublicId(String secureUrl) {
        try {
            String marker = "/upload/";
            int uploadIndex = secureUrl.indexOf(marker);
            if (uploadIndex < 0) return null;
            String afterUpload = secureUrl.substring(uploadIndex + marker.length());
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
            int dotIndex = afterUpload.lastIndexOf('.');
            return dotIndex >= 0 ? afterUpload.substring(0, dotIndex) : afterUpload;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void logFailure(Exception e) {
        System.err.println("[CloudinaryUploadHandler] Cloudinary lỗi, fallback sang Local: "
                + e.getMessage());
    }
}