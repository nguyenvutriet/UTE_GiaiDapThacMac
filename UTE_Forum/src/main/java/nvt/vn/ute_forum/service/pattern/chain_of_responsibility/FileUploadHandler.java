package nvt.vn.ute_forum.service.pattern.chain_of_responsibility;

import org.springframework.web.multipart.MultipartFile;

/**
 * Chain of Responsibility Pattern — Abstract Handler.
 * Xử lý upload và delete file theo chuỗi.
 */
public abstract class FileUploadHandler {

    private FileUploadHandler nextHandler;

    public FileUploadHandler setNext(FileUploadHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    // =========================================================
    // UPLOAD — Thử upload, thất bại thì chuyển tiếp
    // =========================================================
    public final String handle(MultipartFile file, String ownerId) throws Exception {
        try {
            String url = doUpload(file, ownerId);
            if (url != null && !url.isBlank()) return url;
            return passToNext(file, ownerId);
        } catch (Exception e) {
            logFailure(e);
            return passToNext(file, ownerId);
        }
    }

    private String passToNext(MultipartFile file, String ownerId) throws Exception {
        if (nextHandler != null) return nextHandler.handle(file, ownerId);
        throw new Exception("Tất cả phương thức lưu trữ đều thất bại. Vui lòng thử lại.");
    }

    protected abstract String doUpload(MultipartFile file, String ownerId) throws Exception;

    // =========================================================
    // DELETE — Xóa file thật khỏi storage
    // Mỗi handler tự nhận biết URL của mình để xóa đúng chỗ
    // =========================================================
    public final void handleDelete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        if (canHandle(fileUrl)) {
            // Handler này nhận ra URL của mình → tự xóa
            doDelete(fileUrl);
        } else if (nextHandler != null) {
            // Không nhận ra → chuyển tiếp
            nextHandler.handleDelete(fileUrl);
        }
        // Không có handler nào nhận → bỏ qua
    }

    /**
     * Handler tự kiểm tra URL có phải do mình tạo ra không.
     * Cloudinary URL: bắt đầu bằng "https://res.cloudinary.com"
     * Local URL     : bắt đầu bằng "/uploads/"
     */
    protected abstract boolean canHandle(String fileUrl);

    /**
     * Thực hiện xóa file thật khỏi storage.
     */
    protected abstract void doDelete(String fileUrl);

    protected void logFailure(Exception e) {
        System.err.println("[" + getClass().getSimpleName() + "] Upload thất bại: " + e.getMessage());
    }
}