package nvt.vn.ute_forum.controller.forum;

import jakarta.servlet.http.HttpSession;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import nvt.vn.ute_forum.service.CommentService;
import nvt.vn.ute_forum.service.UsersService;
import nvt.vn.ute_forum.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService; // Gọi Service thay vì Repo
    @Autowired
    private VoteCommentRepo voteCommentRepo;


@GetMapping("/{requestId}")
public List<CommentDTO> getComments(@PathVariable String requestId, Principal principal) {
    String currentUserId = "";

    if (principal != null) {
        // Lấy user từ DB dựa trên email trong Principal
        Users user = usersService.getByEmail(principal.getName());
        if (user != null) {
            currentUserId = String.valueOf(user.getId());
        }
    }

    return commentService.getCommentsByRequestId(requestId, currentUserId);
}

    @Autowired
    private UsersService usersService; // Theo ảnh là UsersService (có chữ s)



    @PostMapping("/add")
    public ResponseEntity<CommentDTO> addComment(@RequestBody Map<String, String> payload, Principal principal) {
        try {
            // 1. Lấy dữ liệu từ Request Body
            String content = payload.get("content");
            String requestId = payload.get("requestId"); // Dùng String để khớp với Repo

            // 2. Lấy User đang đăng nhập (Sử dụng Users class có chữ s)
            Users user = usersService.getByEmail(principal.getName());

            // 3. Gọi Service lưu bình luận
            Comment newComment = commentService.saveComment(requestId, user, content);

            // 4. Trả về DTO khớp hoàn toàn với Constructor 4 tham số của bạn
            // Thứ tự: userName, content, date, id
            return ResponseEntity.ok(new CommentDTO(
                    user.getFullName(),          // userName
                    newComment.getContent(),     // content
                    newComment.getDate(),        // date (LocalDateTime)
                    String.valueOf(newComment.getId()) ,// id (ép kiểu về String cho khớp DTO)
                    true
            ));

        } catch (Exception e) {
            // Trả về lỗi nếu có vấn đề (sai ID bài viết, lỗi database...)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
//    @DeleteMapping("/delete/{id}")
//    @ResponseBody
//    public ResponseEntity<?> deleteComment(@PathVariable String id, Principal principal) {
//        // 1. Lấy thông tin người dùng từ Principal (Spring Security)
//        if (principal == null) {
//            return ResponseEntity.status(401).body("Lỗi chưa đăng nập");
//        }
//
//        // 2. Lấy đối tượng Users từ Email/Username
//        Users user = usersService.getByEmail(principal.getName());
//
//        if (user == null) {
//            return ResponseEntity.status(401).body("Không tìm thấy user!");
//        }
//
//        // 3. Tiến hành xóa
//        boolean isDeleted = commentService.deleteCommentIfOwner(id, String.valueOf(user.getId()));
//
//        if (isDeleted) {
//            return ResponseEntity.ok().build();
//        } else {
//            return ResponseEntity.status(403).body("Không được phép xoá!");
//        }
//    }
@DeleteMapping("/delete/{id}")
public ResponseEntity<?> deleteComment(@PathVariable String id, Principal principal) {
    // 1. Lấy thông tin User đầy đủ từ Database dựa vào Email (getName)
    Users user = usersService.getByEmail(principal.getName());

    if (user == null) return ResponseEntity.status(401).body("Chưa đăng nhập!");

    // 2. PHẢI LẤY ID CỦA USER để truyền vào Service
    String currentUserId = String.valueOf(user.getId());

    boolean isDeleted = commentService.deleteCommentIfOwner(id, currentUserId);

    if (isDeleted) {
        return ResponseEntity.ok().build();
    } else {
        // Nếu nó nhảy vào đây là do ownerId != currentUserId đó má!
        return ResponseEntity.status(403).body("Hông phải cmt của má!");
    }
}
}
