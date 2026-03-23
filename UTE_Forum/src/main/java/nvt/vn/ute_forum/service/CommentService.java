package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepo commentRepo;

    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId) {
        return commentRepo.findByRequestId(requestId).stream()
                .map(c -> {
                    // Ép kiểu ID của User về String để so sánh khớp với currentUserId
                    String commentOwnerId = String.valueOf(c.getUser().getId());
                    boolean canDelete = commentOwnerId.equals(currentUserId);

                    return new CommentDTO(
                            c.getUser().getFullName(),
                            c.getContent(),
                            c.getDate(),
                            c.getId(),
                            canDelete
                    );
                })
                .collect(Collectors.toList());
    }

    @Autowired
    private RequestRepo requestRepo; // Tên chuẩn theo ảnh bạn gửi

    @Transactional
    public Comment saveComment(String requestId, Users user, String content) {
        // 1. Tìm bài viết (Request)
        Request postRequest = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        // 2. Tạo đối tượng Comment mới
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setContent(content);
        comment.setUser(user);
        comment.setRequest(postRequest); // Giả sử trong Entity Comment bạn đặt tên field là request
        comment.setDate(LocalDateTime.now());

        // 3. Lưu xuống database
        return commentRepo.save(comment);
    }

//    @Transactional
//    public boolean deleteCommentIfOwner(String commentId, String currentUserId) {
//        return commentRepo.findById(commentId).map(comment -> {
//            // Kiểm tra xem ID người xóa có khớp với ID người tạo cmt không
//            if (String.valueOf(comment.getUser().getId()).equals(currentUserId)) {
//                commentRepo.delete(comment);
//                return true;
//            }
//            return false;
//        }).orElse(false);
//    }
@Transactional
public boolean deleteCommentIfOwner(String commentId, String currentUserId) {
    return commentRepo.findById(commentId).map(comment -> {
        // .trim() để loại bỏ mọi khoảng trắng thừa thãi ở 2 đầu
        String ownerId = String.valueOf(comment.getUser().getId()).trim();
        String visitorId = currentUserId.trim();

        if (ownerId.equals(visitorId)) {
            commentRepo.delete(comment);
            return true;
        }
        return false;
    }).orElse(false);
}

}
