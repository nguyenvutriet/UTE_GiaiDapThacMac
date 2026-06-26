package nvt.vn.ute_forum.service.pattern.template_method;

import nvt.vn.ute_forum.model.Users;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Đối tượng chứa toàn bộ tham số cần thiết cho luồng xử lý feedback.
 * Được truyền vào executeTemplateMethod() của FeedbackHandler.
 */
public class FeedbackParams {

    // Dùng cho cả Submit và Update
    private String subject;
    private String description;
    private String location;
    private List<String> categoryIds;
    private String departmentId;
    private String privacy;
    private MultipartFile[] attachments;
    private Users user;

    // Chỉ dùng cho Update
    private String requestId;

    // =========================================================
    // Constructor cho Submit
    // =========================================================
    public static FeedbackParams forSubmit(String subject,
                                           String description,
                                           String location,
                                           List<String> categoryIds,
                                           String departmentId,
                                           String privacy,
                                           MultipartFile[] attachments,
                                           Users user) {
        FeedbackParams params = new FeedbackParams();
        params.subject = subject;
        params.description = description;
        params.location = location;
        params.categoryIds = categoryIds;
        params.departmentId = departmentId;
        params.privacy = privacy;
        params.attachments = attachments;
        params.user = user;
        return params;
    }

    // =========================================================
    // Constructor cho Update
    // =========================================================
    public static FeedbackParams forUpdate(String requestId,
                                           String subject,
                                           String description,
                                           String location,
                                           List<String> categoryIds,
                                           String departmentId,
                                           String privacy,
                                           MultipartFile[] attachments,
                                           Users user) {
        FeedbackParams params = new FeedbackParams();
        params.requestId = requestId;
        params.subject = subject;
        params.description = description;
        params.location = location;
        params.categoryIds = categoryIds;
        params.departmentId = departmentId;
        params.privacy = privacy;
        params.attachments = attachments;
        params.user = user;
        return params;
    }

    // =========================================================
    // Getters
    // =========================================================
    public String getSubject()       { return subject; }
    public String getDescription()   { return description; }
    public String getLocation()      { return location; }
    public List<String> getCategoryIds() { return categoryIds; }
    public String getDepartmentId()  { return departmentId; }
    public String getPrivacy()       { return privacy; }
    public MultipartFile[] getAttachments() { return attachments; }
    public Users getUser()           { return user; }
    public String getRequestId()     { return requestId; }
}