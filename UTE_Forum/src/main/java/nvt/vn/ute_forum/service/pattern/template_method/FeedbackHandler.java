package nvt.vn.ute_forum.service.pattern.template_method;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.service.FileAttachmentService;
import nvt.vn.ute_forum.service.NotificationService;
import nvt.vn.ute_forum.service.RequestStatusHistoryService;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstract class định nghĩa Template Method Pattern cho luồng xử lý feedback.
 *
 * Cấu trúc:
 *   executeTemplateMethod()     — FINAL    : Khung quy trình bất biến
 *   validateCommonInput()       — CONCRETE : Validate chung (dùng chung cả Submit lẫn Update)
 *   validateSpecificInput()     — ABSTRACT : Validate đặc thù từng luồng
 *   saveRequest()               — ABSTRACT : Insert mới hoặc Update tuỳ subclass
 *   saveHistory()               — ABSTRACT : Submit ghi lịch sử, Update bỏ qua
 *   saveFiles()                 — ABSTRACT : Submit dùng save, Update dùng replace
 *   sendNotification()          — ABSTRACT : Nội dung thông báo khác nhau
 *   logEvent()                  — HOOK     : Tuỳ chọn override, mặc định trống
 */
public abstract class FeedbackHandler {

    protected final RequestStatusHistoryService statusHistoryService;
    protected final FileAttachmentService fileAttachmentService;
    protected final NotificationService notificationService;

    protected FeedbackHandler(RequestStatusHistoryService statusHistoryService,
                              FileAttachmentService fileAttachmentService,
                              NotificationService notificationService) {
        this.statusHistoryService = statusHistoryService;
        this.fileAttachmentService = fileAttachmentService;
        this.notificationService = notificationService;
    }

    // =========================================================
    // TEMPLATE METHOD — FINAL: Khung quy trình bất biến
    // =========================================================
    public final void executeTemplateMethod(FeedbackParams params) {

        // Bước 1a: Validate dữ liệu chung [CONCRETE]
        validateCommonInput(params);

        // Bước 1b: Validate đặc thù từng luồng [ABSTRACT]
        validateSpecificInput(params);

        // Bước 2: Ghi nhận/Cập nhật thông tin Request vào CSDL [ABSTRACT]
        Request savedRequest = saveRequest(params);

        // Bước 3: Ghi nhận lịch sử trạng thái [ABSTRACT]
        saveHistory(savedRequest);

        // Bước 4: Xử lý tệp tin đính kèm [ABSTRACT]
        saveFiles(savedRequest, params.getAttachments());

        // Bước 5: Phát thông báo đến các bên liên quan [ABSTRACT]
        sendNotification(savedRequest, params);

        // Bước 6: Móc nối mở rộng ghi log hệ thống [HOOK]
        logEvent(savedRequest);
    }

    // =========================================================
    // CONCRETE METHOD — Dùng chung cả Submit lẫn Update
    // =========================================================

    /**
     * Validate các trường bắt buộc giống nhau ở cả 2 luồng:
     * subject, description, privacy.
     */
    protected void validateCommonInput(FeedbackParams params) {
        if (params.getSubject() == null || params.getSubject().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tiêu đề.");
        }
        if (params.getDescription() == null || params.getDescription().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung mô tả.");
        }
        if (!"public".equals(params.getPrivacy()) && !"department".equals(params.getPrivacy())) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn chế độ gửi: Công khai hoặc Gửi đến phòng ban."
            );
        }
    }

    // =========================================================
    // ABSTRACT METHODS — Subclass bắt buộc định nghĩa
    // =========================================================

    /**
     * Bước 1b — ABSTRACT: Validate đặc thù.
     * Submit : không có thêm gì.
     * Update : kiểm tra requestId, trạng thái PENDING, đúng chủ sở hữu.
     */
    protected abstract void validateSpecificInput(FeedbackParams params);

    /**
     * Bước 2 — ABSTRACT: Lưu Request vào CSDL.
     * Submit : INSERT bản ghi mới với trạng thái PENDING.
     * Update : UPDATE bản ghi cũ, giữ nguyên trạng thái PENDING.
     */
    protected abstract Request saveRequest(FeedbackParams params);

    /**
     * Bước 3 — ABSTRACT: Ghi lịch sử trạng thái.
     * Submit : gọi createInitialStatus().
     * Update : bỏ qua, không ghi thêm để tránh rác timeline.
     */
    protected abstract void saveHistory(Request savedRequest);

    /**
     * Bước 4 — ABSTRACT: Xử lý file đính kèm.
     * Submit : gọi saveRequestAttachments().
     * Update : gọi replaceRequestAttachments().
     */
    protected abstract void saveFiles(Request savedRequest, MultipartFile[] attachments);

    /**
     * Bước 5 — ABSTRACT: Gửi thông báo.
     * Submit : gửi NEW_FEEDBACK_RECEIVED + FEEDBACK_SUBMITTED_NOTIFICATION.
     * Update : gửi FEEDBACK_PROCESSING_NOTIFICATION cho nhân viên phòng ban.
     */
    protected abstract void sendNotification(Request savedRequest, FeedbackParams params);

    // =========================================================
    // HOOK METHOD — Tuỳ chọn override
    // =========================================================

    /**
     * Bước 6 — HOOK: Ghi log sự kiện.
     * Mặc định không làm gì. Subclass có thể override nếu cần.
     */
    protected void logEvent(Request savedRequest) {
        // Default: do nothing
    }

    // =========================================================
    // HELPER — final, không override
    // =========================================================
    protected final boolean hasAnyAttachment(MultipartFile[] attachments) {
        if (attachments == null) return false;
        for (MultipartFile f : attachments) {
            if (f != null && !f.isEmpty()) return true;
        }
        return false;
    }
}