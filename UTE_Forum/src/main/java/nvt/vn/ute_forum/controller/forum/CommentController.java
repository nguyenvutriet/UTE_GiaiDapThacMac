package nvt.vn.ute_forum.controller.forum;

import jakarta.servlet.http.HttpSession;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentReportRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import nvt.vn.ute_forum.service.CommentService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired private CommentService commentService;
    @Autowired private VoteCommentRepo voteCommentRepo;
    @Autowired private CommentReportRepo reportRepo;
    @Autowired private UsersService usersService;

    // =========================================================================
    // GET — Lấy danh sách comment (đã bao gồm replies lồng nhau)
    // =========================================================================

    @GetMapping("/{requestId}")
    public List<CommentDTO> getComments(@PathVariable String requestId, Principal principal) {
        String currentUserId = "";
        boolean isAdmin = false;

        if (principal != null) {
            Users user = usersService.getByEmail(principal.getName());
            if (user != null) {
                currentUserId = String.valueOf(user.getId());
                isAdmin = "ROLE_ADMIN".equalsIgnoreCase(user.getRole());
            }
        }

        return commentService.getCommentsByRequestId(requestId, currentUserId, isAdmin);
    }

    // =========================================================================
    // POST /add — Đăng comment gốc (giữ nguyên như cũ)
    // =========================================================================

    @PostMapping("/add")
    public ResponseEntity<?> addComment(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập để bình luận"));
            }

            String content   = payload.get("content");
            String requestId = payload.get("requestId");

            Users user = usersService.getByEmail(userDetails.getUsername());
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            Comment newComment = commentService.saveComment(requestId, user, content);

            return ResponseEntity.ok(new CommentDTO(
                    user.getFullName(),
                    newComment.getContent(),
                    newComment.getDate(),
                    String.valueOf(newComment.getId()),
                    true,
                    user.getRole()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // POST /reply — Trả lời một comment (mới)
    //
    // Payload JSON:
    // {
    //   "requestId": "...",   -- ID bài viết
    //   "content":   "...",   -- Nội dung reply
    //   "parentId":  "...",   -- ID comment gốc của thread
    //   "replyId":   "..."    -- ID comment được reply trực tiếp
    // }
    // =========================================================================

    @PostMapping("/reply")
    public ResponseEntity<?> replyComment(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập để trả lời"));
            }

            String content   = payload.get("content");
            String requestId = payload.get("requestId");
            String parentId  = payload.get("parentId");   // ID comment gốc
            String replyId   = payload.get("replyId");    // ID comment được reply

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nội dung không được trống"));
            }
            if (replyId == null || replyId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu replyId"));
            }

            Users user = usersService.getByEmail(userDetails.getUsername());
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            // Nếu frontend không truyền parentId, service sẽ tự resolve
            Comment newReply = commentService.saveReply(requestId, user, content, parentId, replyId);

            // Trả về DTO đầy đủ để frontend render ngay, không cần reload
            CommentDTO dto = new CommentDTO(
                    user.getFullName(),
                    newReply.getContent(),
                    newReply.getDate(),
                    String.valueOf(newReply.getId()),
                    true,  // canDelete = true vì chính mình vừa tạo
                    user.getRole(),
                    newReply.getParentId(),
                    newReply.getReplyId(),
                    newReply.getReplyToUserId(),
                    newReply.getReplyToUserName()
            );

            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // DELETE — Xóa comment (giữ nguyên)
    // =========================================================================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Users user = usersService.getByEmail(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(404).body("User không tồn tại!");

        boolean isDeleted = commentService.deleteCommentIfOwner(id, user.getId());

        return isDeleted
                ? ResponseEntity.ok().body("Đã xoá bình luận!")
                : ResponseEntity.status(403).body("Không được phép xoá!");
    }

    // =========================================================================
    // POST /report — Báo cáo comment (giữ nguyên)
    // =========================================================================

    @PostMapping("/report")
    public ResponseEntity<?> reportComment(@RequestBody Map<String, String> payload, Principal principal) {
        try {
            String commentId = payload.get("commentId");
            String reason    = payload.get("reason");

            if (commentId == null || reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest().body("Thiếu commentId hoặc reason");
            }

            Comment comment = commentService.getCommentById(commentId);
            if (comment == null) {
                return ResponseEntity.badRequest().body("Comment không tồn tại");
            }

            Users reporter = usersService.getByEmail(principal.getName());

            Optional<CommentReport> exist = reportRepo.findExisting(commentId, reporter.getId());
            if (exist.isPresent()) {
                return ResponseEntity.badRequest().body("Bạn đã báo cáo bình luận này rồi!");
            }

            CommentReport report = new CommentReport();
            report.setId(UUID.randomUUID().toString());
            report.setComment(comment);
            report.setStudent(reporter);
            report.setReason(reason);
            report.setStatus("pending");
            report.setCreatedAt(LocalDateTime.now());
            report.setAdmin(null);

            commentService.saveCommentReport(report);
            return ResponseEntity.ok("SUCCESS");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: " + e.getMessage());
        }
    }
}