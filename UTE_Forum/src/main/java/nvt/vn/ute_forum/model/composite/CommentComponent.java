package nvt.vn.ute_forum.model.composite;

import nvt.vn.ute_forum.dto.CommentDTO;

import java.util.List;

/**
 * COMPOSITE PATTERN - Component interface
 *
 * Mọi node trong cây comment đều implement interface này,
 * cho phép client xử lý leaf (comment gốc không có reply)
 * và composite (comment có replies) theo cùng 1 cách.
 */
public interface CommentComponent {

    String getId();

    String getContent();

    String getUserName();

    /**
     * Thêm comment con vào node hiện tại.
     * Leaf node sẽ throw UnsupportedOperationException.
     */
    void addChild(CommentComponent child);

    /**
     * Lấy danh sách comment con.
     * Leaf node trả về danh sách rỗng.
     */
    List<CommentComponent> getChildren();

    /**
     * Kiểm tra node hiện tại có phải là leaf không
     * (leaf = bình luận gốc không có reply nào).
     */
    boolean isLeaf();

    /**
     * Chuyển sang DTO để trả về cho frontend.
     * Mỗi loại node tự quyết định cách chuyển đổi (Strategy-like).
     */
    CommentDTO toDTO();
}