package nvt.vn.ute_forum.model.builder;

import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BUILDER PATTERN - CommentBuilder
 *
 * Giải quyết vấn đề: Comment có nhiều trường tùy chọn (parentId, replyId,
 * parentComment...) nên constructor sẽ rất dài và dễ nhầm thứ tự tham số.
 *
 * Builder cho phép tạo Comment theo từng bước rõ ràng:
 *   new CommentBuilder(request, user, content)
 *       .asReply(parentId, replyId, replyToUser)   // Nếu là reply
 *       .build();
 *
 * Kết hợp với Composite Pattern: service dùng Builder để tạo Comment,
 * sau đó CommentNode wraps Comment đó vào cây composite.
 */
public class CommentBuilder {

    // === Bắt buộc ===
    private final Request request;
    private final Users user;
    private final String content;

    // === Tùy chọn (cho reply) ===
    private String parentId;       // ID của comment gốc trong thread
    private String replyId;        // ID của comment được reply trực tiếp
    private String replyToUserId;  // ID user được mention "@"
    private String replyToUserName;// Tên hiển thị trong "@mention"
    private Comment parentComment; // Entity JPA của comment cha (để mapping @ManyToOne)

    /**
     * Constructor bắt buộc — đủ để tạo một comment gốc (top-level)
     */
    public CommentBuilder(Request request, Users user, String content) {
        this.request = request;
        this.user = user;
        this.content = content;
    }

    /**
     * Thiết lập thông tin reply — chỉ gọi khi comment này là một reply
     *
     * @param parentId       ID comment gốc của toàn thread (không thay đổi dù reply nhiều cấp)
     * @param replyId        ID comment được reply trực tiếp (cấp ngay trên)
     * @param replyToUser    User đang được reply để lấy tên + ID cho "@mention"
     */
    public CommentBuilder asReply(String parentId, String replyId, Users replyToUser, Comment parentCommentEntity) {
        this.parentId = parentId;
        this.replyId = replyId;
        this.replyToUserId = replyToUser != null ? replyToUser.getId() : null;
        this.replyToUserName = replyToUser != null ? replyToUser.getFullName() : null;
        this.parentComment = parentCommentEntity;
        return this; // Fluent API
    }

    /**
     * Build ra đối tượng Comment hoàn chỉnh
     */
    public Comment build() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setContent(content);
        comment.setUser(user);
        comment.setRequest(request);
        comment.setDate(LocalDateTime.now());
        comment.setIsActive(true);

        // Chỉ gán nếu là reply
        if (parentId != null) {
            comment.setParentId(parentId);
            comment.setReplyId(replyId);
            comment.setReplyToUserId(replyToUserId);
            comment.setReplyToUserName(replyToUserName);
            comment.setParentComment(parentComment);
        }

        return comment;
    }
}