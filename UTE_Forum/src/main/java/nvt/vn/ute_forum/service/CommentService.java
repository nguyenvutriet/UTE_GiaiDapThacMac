package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.CommentReportRepo;
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

    @Autowired
    private UsersService usersService;

    //*
    @Autowired
    private CommentReportRepo reportRepo;

    // Lấy comment theo ID
    public Comment getCommentById(String commentId) {
        return commentRepo.findById(commentId)
                .orElse(null); // trả về null nếu không tìm thấy
    }

    // Lưu report
    @Transactional
    public void saveCommentReport(CommentReport report) {
        reportRepo.save(report);
    }
    //*


//    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId) {
//        return commentRepo.findActiveByRequestId(requestId).stream()
//                .map(c -> {
//
//                    String commentOwnerId = String.valueOf(c.getUser().getId());
//                    boolean canDelete = commentOwnerId.equals(currentUserId);
//
//                    CommentDTO dto = new CommentDTO(
//                            c.getUser().getFullName(),
//                            c.getContent(),
//                            c.getDate(),
//                            c.getId(),
//                            canDelete,
//                            c.getUser().getRole() // <-- Truyền thêm Role của User vào đây
//                    );
//
//                    Optional<VoteComment> userVote = Optional.empty();
//
//                    if (currentUserId != null && !currentUserId.isEmpty()) {
//                        userVote = voteCommentRepo
//                                .findByIdUserIdAndIdCommentId(currentUserId, c.getId());
//                    }
//
//                    dto.setReactionType(
//                            userVote.map(v -> v.getType().name()).orElse(null)
//                    );
//
//                    // 🔥 2. LẤY TỔNG REACTION
//                    List<Object[]> raw = voteCommentRepo.countReactionsByCommentId(c.getId());
//
//                    Map<String, Long> reactions = new HashMap<>();
//
//                    for (Object[] row : raw) {
//                        reactions.put(row[0].toString(), (Long) row[1]);
//                    }
//
//                    dto.setReactions(reactions);
//
//                    return dto;
//
//                })
//                .collect(Collectors.toList());
//    }

//    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId) {
//        return commentRepo.findActiveByRequestId(requestId).stream()
//                .map(c -> {
//                    String commentOwnerId = c.getUser() != null ? String.valueOf(c.getUser().getId()) : null;
//                    boolean canDelete = currentUserId != null && currentUserId.equals(commentOwnerId);
//
//                    CommentDTO dto = new CommentDTO(
//                            c.getUser() != null ? c.getUser().getFullName() : "Ẩn danh",
//                            c.getContent(),
//                            c.getDate(),
//                            c.getId(),
//                            canDelete,
//                            c.getUser() != null ? c.getUser().getRole() : "ROLE_STUDENT"
//                    );
//
//                    Optional<VoteComment> userVote = Optional.empty();
//                    if (currentUserId != null && !currentUserId.isEmpty()) {
//                        userVote = voteCommentRepo.findByIdUserIdAndIdCommentId(currentUserId, c.getId());
//                    }
//
//                    dto.setReactionType(userVote.map(v -> v.getType().name()).orElse(null));
//
//                    List<Object[]> raw = voteCommentRepo.countReactionsByCommentId(c.getId());
//                    Map<String, Long> reactions = new HashMap<>();
//                    for (Object[] row : raw) {
//                        reactions.put(row[0].toString(), (Long) row[1]);
//                    }
//                    dto.setReactions(reactions);
//
//                    // ===== FIX GỐC Ở ĐÂY =====
//                    boolean reportedByCurrentUser = false;
//                    if (currentUserId != null && !currentUserId.isEmpty()) {
//                        reportedByCurrentUser = reportRepo.existsByComment_IdAndStudent_Id(c.getId(), currentUserId);
//                    }
//                    dto.setReportedByCurrentUser(reportedByCurrentUser);
//
//                    // Nếu DTO đã có field isActive thì set luôn
//                    dto.setIsActive(c.getIsActive());
//
//                    return dto;
//                })
//                .collect(Collectors.toList());
//    }

    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId, boolean isAdmin) {
        List<Comment> comments = isAdmin
                ? commentRepo.findByRequestId(requestId)
                : commentRepo.findActiveByRequestId(requestId);

        return comments.stream()
                .map(c -> {
                    String commentOwnerId = c.getUser() != null ? String.valueOf(c.getUser().getId()) : null;
                    boolean canDelete = currentUserId != null && currentUserId.equals(commentOwnerId);

                    CommentDTO dto = new CommentDTO(
                            c.getUser() != null ? c.getUser().getFullName() : "Ẩn danh",
                            c.getContent(),
                            c.getDate(),
                            c.getId(),
                            canDelete,
                            c.getUser() != null ? c.getUser().getRole() : "ROLE_STUDENT"
                    );

                    Optional<VoteComment> userVote = Optional.empty();
                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        userVote = voteCommentRepo.findByIdUserIdAndIdCommentId(currentUserId, c.getId());
                    }

                    dto.setReactionType(userVote.map(v -> v.getType().name()).orElse(null));

                    List<Object[]> raw = voteCommentRepo.countReactionsByCommentId(c.getId());
                    Map<String, Long> reactions = new HashMap<>();
                    for (Object[] row : raw) {
                        reactions.put(row[0].toString(), (Long) row[1]);
                    }
                    dto.setReactions(reactions);

                    boolean reportedByCurrentUser = false;
                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        reportedByCurrentUser = reportRepo.existsByComment_IdAndStudent_Id(c.getId(), currentUserId);
                    }
                    dto.setReportedByCurrentUser(reportedByCurrentUser);

                    dto.setIsActive(c.getIsActive());

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
        comment.setRequest(postRequest);
        comment.setDate(LocalDateTime.now());

        // 3. Lưu xuống database
        return commentRepo.save(comment);
    }


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

//    @Transactional
//    public boolean deleteCommentAsAdmin(String commentId) {
//        return commentRepo.findById(commentId).map(comment -> {
//            commentRepo.delete(comment);
//            return true;
//        }).orElse(false); // Nếu không tìm thấy comment trả về false
//    }
    public void deleteCommentAsAdmin(String commentId) {
        commentRepo.findById(commentId).ifPresent(c -> {
            c.setIsActive(false); // ẩn
            commentRepo.save(c);
        });
    }

    @Transactional
    public Comment save(Comment comment) {
        return commentRepo.save(comment);
    }

}
