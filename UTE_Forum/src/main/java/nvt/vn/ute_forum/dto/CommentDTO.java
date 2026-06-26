package nvt.vn.ute_forum.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentDTO {

    private String userName;
    private String content;
    private LocalDateTime date;
    private String id;
    private boolean canDelete;
    private Boolean isActive;
    private boolean reportedByCurrentUser;
    private String reactionType;
    private Map<String, Long> reactions;
    private String userRole;

    // =========================================================
    // REPLY FIELDS — Thêm mới
    // =========================================================

    /** ID comment gốc của toàn thread (null nếu đây là comment gốc) */
    private String parentId;

    /** ID comment được reply trực tiếp (null nếu đây là comment gốc) */
    private String replyId;

    /** ID user được mention "@" */
    private String replyToUserId;

    /** Tên user được mention — dùng để render "@TênNgười" trong UI */
    private String replyToUserName;

    /** Danh sách reply con — Composite Pattern: được điền bởi CommentNode.toDTO() */
    private List<CommentDTO> replies = new ArrayList<>();

    /** Tổng số reply — dùng để render "Xem N trả lời" */
    private int replyCount;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CommentDTO() {}

    /** Constructor cho comment gốc (không có reply info) */
    public CommentDTO(String userName, String content, LocalDateTime date,
                      String id, boolean canDelete, String userRole) {
        this.userName = userName;
        this.content = content;
        this.date = date;
        this.id = id;
        this.canDelete = canDelete;
        this.userRole = userRole;
    }

    /** Constructor đầy đủ cho reply */
    public CommentDTO(String userName, String content, LocalDateTime date,
                      String id, boolean canDelete, String userRole,
                      String parentId, String replyId,
                      String replyToUserId, String replyToUserName) {
        this(userName, content, date, id, canDelete, userRole);
        this.parentId = parentId;
        this.replyId = replyId;
        this.replyToUserId = replyToUserId;
        this.replyToUserName = replyToUserName;
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public boolean isReportedByCurrentUser() { return reportedByCurrentUser; }
    public void setReportedByCurrentUser(boolean reportedByCurrentUser) { this.reportedByCurrentUser = reportedByCurrentUser; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public Map<String, Long> getReactions() { return reactions; }
    public void setReactions(Map<String, Long> reactions) { this.reactions = reactions; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    // --- Reply fields ---
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public String getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }

    public List<CommentDTO> getReplies() { return replies; }
    public void setReplies(List<CommentDTO> replies) { this.replies = replies; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    /** Tiện ích: kiểm tra comment này có phải là reply không */
    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }
}