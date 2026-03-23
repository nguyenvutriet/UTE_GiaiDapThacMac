package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private VoteCommentRepo voteCommentRepo;

    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId) {
        return commentRepo.findByRequestId(requestId).stream()
                .map(c -> {

                    String commentOwnerId = String.valueOf(c.getUser().getId());
                    boolean canDelete = commentOwnerId.equals(currentUserId);

                    CommentDTO dto = new CommentDTO(
                            c.getUser().getFullName(),
                            c.getContent(),
                            c.getDate(),
                            c.getId(),
                            canDelete
                    );

                    Optional<VoteComment> userVote = Optional.empty();

                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        userVote = voteCommentRepo
                                .findByIdUserIdAndIdCommentId(currentUserId, c.getId());
                    }

                    dto.setReactionType(
                            userVote.map(v -> v.getType().name()).orElse(null)
                    );

                    // 🔥 2. LẤY TỔNG REACTION
                    List<Object[]> raw = voteCommentRepo.countReactionsByCommentId(c.getId());

                    Map<String, Long> reactions = new HashMap<>();

                    for (Object[] row : raw) {
                        reactions.put(row[0].toString(), (Long) row[1]);
                    }

                    dto.setReactions(reactions);

                    return dto;

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
