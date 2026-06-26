package nvt.vn.ute_forum.service.pattern.chain_of_responsibility;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory tạo chuỗi Chain of Responsibility cho upload file.
 *
 * Chuỗi:
 *   CloudinaryUploadHandler → LocalUploadHandler
 *
 * FileAttachmentService dùng factory này để lấy handler đầu chuỗi,
 * sau đó gọi handler.handle(file, ownerId) là xong.
 */
@Component
public class FileUploadChainFactory {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    /**
     * Tạo và trả về handler đầu chuỗi.
     * CloudinaryUploadHandler → LocalUploadHandler
     */
    public FileUploadHandler buildChain() {
        CloudinaryUploadHandler cloudinaryHandler = new CloudinaryUploadHandler(cloudinary);
        LocalUploadHandler localHandler = new LocalUploadHandler(uploadDir);

        // Thiết lập chuỗi: Cloudinary → Local
        cloudinaryHandler.setNext(localHandler);

        return cloudinaryHandler; // Trả về handler đầu chuỗi
    }
}