package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "isactive")
    private Boolean isActive = true;

    // =====================================================================
    // REPLY FIELDS — Thêm mới để hỗ trợ tính năng trả lời bình luận
    // =====================================================================

    /**
     * ID của comment gốc (top-level) trong toàn bộ thread.
     * Không thay đổi dù reply nhiều cấp — luôn trỏ về comment đầu tiên.
     *
     * Ví dụ: A → B → C thì C.parentId = A.id, B.parentId = A.id
     *
     * Dùng để: nhóm tất cả reply vào 1 thread, query "lấy tất cả reply của thread A"
     */
    @Column(name = "parent_id")
    private String parentId;

    /**
     * ID của comment được reply trực tiếp (cấp ngay trên).
     *
     * Ví dụ: A → B → C thì C.replyId = B.id (reply trực tiếp B)
     *
     * Dùng để: hiển thị "@TênNgười" đúng người trong UI
     */
    @Column(name = "reply_id")
    private String replyId;

    /**
     * ID của user được reply (để frontend có thể link đến profile).
     * Lưu riêng vì không cần join Users chỉ để lấy ID/tên khi render.
     */
    @Column(name = "reply_to_user_id")
    private String replyToUserId;

    /**
     * Tên hiển thị của user được reply — lưu sẵn để tránh join thêm bảng.
     * Dùng cho tag "@TênNgười" trong frontend.
     */
    @Column(name = "reply_to_user_name")
    private String replyToUserName;

    // =====================================================================
    // RELATIONSHIPS
    // =====================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * JPA relationship đến comment cha (cấp ngay trên) — dùng cho cascade delete.
     * Lưu ý: đây là parentComment theo nghĩa JPA (cấp ngay trên),
     * còn parentId (String) là ID của comment gốc toàn thread.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    /**
     * Danh sách reply trực tiếp của comment này (cấp ngay dưới).
     * Composite Pattern: comment này đóng vai trò Composite khi có replies.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentComment")
    private List<Comment> replies = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "comment")
    private List<CommentReport> commentReports = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "comment")
    private List<VoteComment> voteComments = new ArrayList<>();

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    public Comment() {}

    public Comment(String id, String content, LocalDateTime date) {
        this.id = id;
        this.content = content;
        this.date = date;
    }

    // =====================================================================
    // GETTERS & SETTERS
    // =====================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // --- Reply fields ---
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public String getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }

    // --- Relationships ---
    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }

    public List<CommentReport> getCommentReports() { return commentReports; }
    public void setCommentReports(List<CommentReport> commentReports) { this.commentReports = commentReports; }

    public List<VoteComment> getVoteComments() { return voteComments; }
    public void setVoteComments(List<VoteComment> voteComments) { this.voteComments = voteComments; }
}