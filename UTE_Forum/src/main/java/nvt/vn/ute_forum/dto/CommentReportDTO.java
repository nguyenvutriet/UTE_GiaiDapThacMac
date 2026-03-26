package nvt.vn.ute_forum.dto;

import java.time.LocalDateTime;

public class CommentReportDTO {
    private String id;
    private String commentContent;   // BÌNH LUẬN
    private String reason;           // LÝ DO
    private String reporter;
    private String admin;           // admin duyệt// NGƯỜI BÁO CÁO
    private String status;// TRẠNG THÁI
    private String requestId;   // 🔥 thêm
    private String commentId;
    private String source;           // NGUỒN (để trống)
    private LocalDateTime createdAt; // thời gian tạo report

    private Boolean commentActive;

    public CommentReportDTO(
            String id,
            String commentContent,
            String reason,
            String reporter,
            String status,
            String requestId,
            String commentId,
            LocalDateTime createdAt,
            String admin,
            Boolean commentActive
    ) {
        this.id = id;
        this.commentContent = commentContent;
        this.reason = reason;
        this.reporter = reporter;
        this.status = status;
        this.createdAt = createdAt;
        this.admin = admin;

        // Lưu lại để controller dùng
        this.requestId = requestId;  // 🔥
        this.commentId = commentId;  // 🔥
        this.commentActive = commentActive;

        // Link xem bình luận
        this.source = "/admin/forum?requestId=" + requestId + "&commentId=" + commentId;    }

    // getter & setter

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAdmin() { return admin; }
    public void setAdmin(String admin) { this.admin = admin; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public Boolean getCommentActive() { return commentActive; }
    public void setCommentActive(Boolean commentActive) { this.commentActive = commentActive; }
}