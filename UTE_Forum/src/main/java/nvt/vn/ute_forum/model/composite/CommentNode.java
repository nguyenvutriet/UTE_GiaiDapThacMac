package nvt.vn.ute_forum.model.composite;

import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COMPOSITE PATTERN - Composite node
 *
 * Đại diện cho một comment có thể chứa các reply con.
 * Cả comment gốc (top-level) lẫn reply đều dùng class này —
 * sự khác biệt nằm ở việc comment gốc không có parentId/replyId.
 *
 * Thay vì tách riêng Leaf/Composite thành 2 class cứng nhắc,
 * ta dùng isLeaf() để kiểm tra runtime, giúp frontend
 * linh hoạt render cả 2 loại mà không cần biết kiểu cụ thể.
 */
public class CommentNode implements CommentComponent {

    private final Comment comment;
    private final CommentDTO dto;
    private final List<CommentComponent> children = new ArrayList<>();

    public CommentNode(Comment comment, CommentDTO dto) {
        this.comment = comment;
        this.dto = dto;
    }

    @Override
    public String getId() {
        return comment.getId();
    }

    @Override
    public String getContent() {
        return comment.getContent();
    }

    @Override
    public String getUserName() {
        return dto.getUserName();
    }

    @Override
    public void addChild(CommentComponent child) {
        children.add(child);
    }

    @Override
    public List<CommentComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Leaf khi không có reply nào → frontend không render nút "Xem trả lời"
     */
    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public CommentDTO toDTO() {
        // Gắn replies vào DTO để trả về cho frontend
        List<CommentDTO> replyDTOs = children.stream()
                .map(CommentComponent::toDTO)
                .toList();
        dto.setReplies(replyDTOs);
        dto.setReplyCount(children.size());
        return dto;
    }
}