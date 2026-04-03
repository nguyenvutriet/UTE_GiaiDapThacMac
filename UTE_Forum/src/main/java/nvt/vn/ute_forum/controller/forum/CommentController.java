package nvt.vn.ute_forum.controller.forum;

import jakarta.servlet.http.HttpSession;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.CommentReportRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import nvt.vn.ute_forum.service.CommentService;
import nvt.vn.ute_forum.service.UsersService;
import nvt.vn.ute_forum.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService; // Gọi Service thay vì Repo
    @Autowired
    private VoteCommentRepo voteCommentRepo;

    @Autowired
    private CommentReportRepo reportRepo;

//    @GetMapping("/{requestId}")
//    public List<CommentDTO> getComments(@PathVariable String requestId, Principal principal) {
//        String currentUserId = "";
//
//        if (principal != null) {
//            // Lấy user từ DB dựa trên email trong Principal
//            Users user = usersService.getByEmail(principal.getName());
//            if (user != null) {
//                currentUserId = String.valueOf(user.getId());
//            }
//            System.out.println("Role thực tế trong DB: " + user.getRole()); // Kiểm tra hàm getRole() này
//        }
//
//        return commentService.getCommentsByRequestId(requestId, currentUserId);
//    }

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

    @Autowired
    private UsersService usersService; // Theo ảnh là UsersService (có chữ s)



    @PostMapping("/add")
    public ResponseEntity<?> addComment(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 1. Kiểm tra nếu user chưa đăng nhập (Security trả về null)
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập để bình luận"));
            }

            // 2. Lấy dữ liệu
            String content = payload.get("content");
            String requestId = payload.get("requestId");

            // 3. Lấy đối tượng Users dựa trên username (là email trong UserPrincipal của bạn)
            Users user = usersService.getByEmail(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 4. Lưu vào database
            Comment newComment = commentService.saveComment(requestId, user, content);

            // 5. Trả về DTO khớp hoàn toàn với cấu trúc JS đang nhận
            return ResponseEntity.ok(new CommentDTO(
                    user.getFullName(),                 // userName
                    newComment.getContent(),            // content
                    newComment.getDate(),               // date
                    String.valueOf(newComment.getId()), // id (String)
                    true,                               // status
                    user.getRole()                      // 🔥 TRUYỀN THÊM ROLE Ở ĐÂY (Giả sử hàm là getRole())

            ));


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) { // Đồng bộ với /view

        // 1. Kiểm tra đăng nhập (Tránh lỗi Null nếu quên cấu hình Security)
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập thì xóa bằng niềm tin hả má?");
        }

        // 2. Lấy User từ Email (getUsername trả về email trong UserPrincipal của bạn)
        Users user = usersService.getByEmail(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(404).body("User hông tồn tại!");

        // 3. Thực hiện xóa (Truyền ID của người đang login vào để Service kiểm tra)
        boolean isDeleted = commentService.deleteCommentIfOwner(id, user.getId());

        if (isDeleted) {
            return ResponseEntity.ok().body("Đã xoá bình luận!");
        } else {
            // Trả về 403 nếu cố tình xóa comment của người khác
            return ResponseEntity.status(403).body("Không được phép xoá!");
        }
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportComment(@RequestBody Map<String, String> payload, Principal principal) {
        try {
            // 1. Lấy commentId và lý do báo cáo từ payload
            String commentId = payload.get("commentId");
            String reason = payload.get("reason");

            if (commentId == null || reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest().body("Thiếu commentId hoặc reason");
            }

            // 2. Lấy comment từ DB
            Comment comment = commentService.getCommentById(commentId);
            if (comment == null) {
                return ResponseEntity.badRequest().body("Comment không tồn tại");
            }

            // 3. Lấy user đang đăng nhập
            Users reporter = usersService.getByEmail(principal.getName());

            Optional<CommentReport> exist = reportRepo.findExisting(commentId, reporter.getId());
            if (exist.isPresent()) {
                return ResponseEntity.badRequest().body("Bạn đã báo cáo bình luận này rồi!");
            }
            // 4. Tạo CommentReport
            CommentReport report = new CommentReport();
            report.setId(UUID.randomUUID().toString());
            report.setComment(comment);
            report.setStudent(reporter); // Người báo cáo
            report.setReason(reason);
            report.setStatus("pending");
            report.setCreatedAt(LocalDateTime.now());
            report.setAdmin(null); // chưa ai xử lý

            // 5. Lưu report
            commentService.saveCommentReport(report); // Tạo phương thức service để save report

            return ResponseEntity.ok("SUCCESS");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ERROR: " + e.getMessage());
        }
    }
}