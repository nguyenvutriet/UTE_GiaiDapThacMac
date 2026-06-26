package nvt.vn.ute_forum.service.pattern.template_method;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.service.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete class xử lý luồng GỬI feedback mới.
 *
 * Override:
 *   validateSpecificInput() : Submit không có validate thêm → để trống
 *   saveRequest()           : INSERT Request mới, set PENDING
 *   saveHistory()           : Ghi lịch sử trạng thái ban đầu
 *   saveFiles()             : Lưu file đính kèm mới
 *   sendNotification()      : Gửi NEW_FEEDBACK_RECEIVED + FEEDBACK_SUBMITTED_NOTIFICATION
 */
public class SubmitFeedbackHandler extends FeedbackHandler {

    private static final String DEFAULT_DEPARTMENT_ID = "DEP_CTSV";

    private final RequestRepo requestRepo;
    private final DepartmentRepo departmentRepo;
    private final CategoryService categoryService;
    private final IdGeneratorService idGeneratorService;

    public SubmitFeedbackHandler(RequestStatusHistoryService statusHistoryService,
                                 FileAttachmentService fileAttachmentService,
                                 NotificationService notificationService,
                                 RequestRepo requestRepo,
                                 DepartmentRepo departmentRepo,
                                 CategoryService categoryService,
                                 IdGeneratorService idGeneratorService) {
        super(statusHistoryService, fileAttachmentService, notificationService);
        this.requestRepo = requestRepo;
        this.departmentRepo = departmentRepo;
        this.categoryService = categoryService;
        this.idGeneratorService = idGeneratorService;
    }

    // =========================================================
    // Bước 1b — OVERRIDE: Validate đặc thù Submit
    // Submit không có validate thêm ngoài validateCommonInput()
    // =========================================================
    @Override
    protected void validateSpecificInput(FeedbackParams params) {
        // Submit: validateCommonInput() đã đủ, không cần validate thêm
    }

    // =========================================================
    // Bước 2 — OVERRIDE: INSERT Request mới
    // =========================================================
    @Override
    protected Request saveRequest(FeedbackParams params) {
        Department department = resolveTargetDepartment(params.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng ban không tồn tại."));

        Request request = new Request();
        request.setId(idGeneratorService.nextRequestId());
        request.setCurrentStatus("PENDING");
        request.setTimeCreate(LocalDateTime.now());
        request.setUser(params.getUser());
        request.setSubject(params.getSubject());
        request.setDescription(params.getDescription());
        request.setLocation(normalizeOptionalText(params.getLocation()));
        request.setPostStatus("public".equals(params.getPrivacy()) ? "PUBLIC" : "PRIVATE");
        request.setDepartment(department);
        request.getCategories().clear();
        request.getCategories().addAll(resolveCategories(params.getCategoryIds()));

        return requestRepo.save(request);
    }

    // =========================================================
    // Bước 3 — OVERRIDE: Ghi lịch sử trạng thái ban đầu
    // =========================================================
    @Override
    protected void saveHistory(Request savedRequest) {
        statusHistoryService.createInitialStatus(savedRequest, savedRequest.getCurrentStatus());
    }

    // =========================================================
    // Bước 4 — OVERRIDE: Lưu file đính kèm mới
    // =========================================================
    @Override
    protected void saveFiles(Request savedRequest, MultipartFile[] attachments) {
        if (!hasAnyAttachment(attachments)) return;
        try {
            fileAttachmentService.saveRequestAttachments(savedRequest, attachments);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Không thể lưu tệp đính kèm.", e);
        }
    }

    // =========================================================
    // Bước 5 — OVERRIDE: Gửi 2 thông báo cho Submit
    // =========================================================
    @Override
    protected void sendNotification(Request savedRequest, FeedbackParams params) {
        Department department = savedRequest.getDepartment();
        List<Users> deptStaffs = department.getUsers() == null
                ? Collections.emptyList()
                : department.getUsers().stream()
                .filter(u -> "ROLE_DEPARTMENT".equals(u.getRole()))
                .toList();

        // Thông báo cho nhân viên phòng ban
        notificationService.createNotificationForUsers(
                "NEW_FEEDBACK_RECEIVED",
                "Góp ý mới gửi đến phòng ban",
                "Góp ý: " + savedRequest.getSubject(),
                deptStaffs,
                savedRequest.getId()
        );

        // Thông báo cho sinh viên gửi
        notificationService.createNotificationForUsers(
                "FEEDBACK_SUBMITTED_NOTIFICATION",
                "Gửi phản hồi thành công",
                "Bạn đã gửi phản hồi thành công.",
                List.of(params.getUser()),
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