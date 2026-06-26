package nvt.vn.ute_forum.service.pattern.template_method;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.service.CategoryService;
import nvt.vn.ute_forum.service.FileAttachmentService;
import nvt.vn.ute_forum.service.NotificationService;
import nvt.vn.ute_forum.service.RequestStatusHistoryService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete class xử lý luồng CẬP NHẬT feedback đã gửi.
 *
 * Override:
 *   validateSpecificInput() : Kiểm tra requestId, trạng thái PENDING, đúng chủ sở hữu
 *   saveRequest()           : UPDATE bản ghi cũ, giữ nguyên trạng thái PENDING
 *   saveHistory()           : Bỏ qua, không ghi thêm để tránh rác timeline
 *   saveFiles()             : Thay thế file đính kèm cũ bằng file mới
 *   sendNotification()      : Gửi FEEDBACK_PROCESSING_NOTIFICATION cho nhân viên phòng ban
 */
public class UpdateFeedbackHandler extends FeedbackHandler {

    private static final String DEFAULT_DEPARTMENT_ID = "DEP_CTSV";

    private final RequestRepo requestRepo;
    private final DepartmentRepo departmentRepo;
    private final CategoryService categoryService;

    // Dùng ThreadLocal để truyền Request đã load từ validateSpecificInput sang saveRequest
    // tránh query DB 2 lần
    private final ThreadLocal<Request> contextRequestHolder = new ThreadLocal<>();

    public UpdateFeedbackHandler(RequestStatusHistoryService statusHistoryService,
                                 FileAttachmentService fileAttachmentService,
                                 NotificationService notificationService,
                                 RequestRepo requestRepo,
                                 DepartmentRepo departmentRepo,
                                 CategoryService categoryService) {
        super(statusHistoryService, fileAttachmentService, notificationService);
        this.requestRepo = requestRepo;
        this.departmentRepo = departmentRepo;
        this.categoryService = categoryService;
    }

    // =========================================================
    // Bước 1b — OVERRIDE: Validate đặc thù Update
    // Kiểm tra requestId, trạng thái PENDING, đúng chủ sở hữu
    // =========================================================
    @Override
    protected void validateSpecificInput(FeedbackParams params) {
        if (params.getRequestId() == null || params.getRequestId().isBlank()) {
            throw new IllegalArgumentException("Mã góp ý không hợp lệ.");
        }

        Request existingRequest = requestRepo
                .findByIdAndUser_Id(params.getRequestId(), params.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy góp ý chỉnh sửa hợp lệ."
                ));

        if (!"PENDING".equals(existingRequest.getCurrentStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ được sửa góp ý khi đang ở trạng thái chờ tiếp nhận."
            );
        }

        // Cache lại để saveRequest() dùng, tránh query DB 2 lần
        contextRequestHolder.set(existingRequest);
    }

    // =========================================================
    // Bước 2 — OVERRIDE: UPDATE bản ghi cũ
    // =========================================================
    @Override
    protected Request saveRequest(FeedbackParams params) {
        try {
            Request request = contextRequestHolder.get();
            if (request == null) {
                throw new IllegalStateException("Hệ thống mất ngữ cảnh dữ liệu.");
            }

            Department department = resolveTargetDepartment(params.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Phòng ban không tồn tại."));

            // Cập nhật nội dung, giữ nguyên trạng thái PENDING
            request.setSubject(params.getSubject());
            request.setDescription(params.getDescription());
            request.setLocation(normalizeOptionalText(params.getLocation()));
            request.setPostStatus("public".equals(params.getPrivacy()) ? "PUBLIC" : "PRIVATE");
            request.setDepartment(department);
            request.getCategories().clear();
            request.getCategories().addAll(resolveCategories(params.getCategoryIds()));

            return requestRepo.save(request);
        } finally {
            // Luôn giải phóng ThreadLocal dù thành công hay lỗi
            contextRequestHolder.remove();
        }
    }

    // =========================================================
    // Bước 3 — OVERRIDE: Bỏ qua, không ghi thêm lịch sử
    // Tránh tạo rác trong timeline khi sinh viên chỉ sửa nội dung
    // =========================================================
    @Override
    protected void saveHistory(Request savedRequest) {
        // Do nothing - Không ghi nhận thêm dòng lịch sử trạng thái khi sinh viên sửa ở PENDING
    }

    // =========================================================
    // Bước 4 — OVERRIDE: Thay thế file đính kèm cũ bằng file mới
    // =========================================================
    @Override
    protected void saveFiles(Request savedRequest, MultipartFile[] attachments) {
        if (!hasAnyAttachment(attachments)) return;
        try {
            fileAttachmentService.replaceRequestAttachments(savedRequest, attachments);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Có lỗi xảy ra trong quá trình cập nhật tệp tin.", e);
        }
    }

    // =========================================================
    // Bước 5 — OVERRIDE: Chỉ gửi thông báo cho nhân viên phòng ban
    // =========================================================
    @Override
    protected void sendNotification(Request savedRequest, FeedbackParams params) {
        Department department = savedRequest.getDepartment();
        List<Users> deptStaffs = department.getUsers() == null
                ? Collections.emptyList()
                : department.getUsers().stream()
                .filter(u -> "ROLE_DEPARTMENT".equals(u.getRole()))
                .toList();

        notificationService.createNotificationForUsers(
                "FEEDBACK_PROCESSING_NOTIFICATION",
                "Góp ý đã được cập nhật",
                "Sinh viên đã cập nhật lại góp ý: \"" + savedRequest.getSubject() + "\".",
                deptStaffs,
                savedRequest.getId()
        );
    }

    // =========================================================
    // HELPER
    // =========================================================
    private Optional<Department> resolveTargetDepartment(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            return departmentRepo.findById(DEFAULT_DEPARTMENT_ID);
        }
        Optional<Department> found = departmentRepo.findById(departmentId);
        return found.isPresent() ? found : departmentRepo.findById(DEFAULT_DEPARTMENT_ID);
    }

    private List<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyList();
        List<String> cleanedIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .toList();
        return categoryService.getAllCategories().stream()
                .filter(c -> cleanedIds.contains(c.getId()))
                .toList();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}