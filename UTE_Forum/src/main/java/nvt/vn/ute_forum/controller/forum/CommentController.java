package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.service.CommentService;
import nvt.vn.ute_forum.service.UsersService;
import nvt.vn.ute_forum.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{requestId}")
    public List<CommentDTO> getComments(@PathVariable String requestId) {
        return commentService.getCommentsByRequestId(requestId);
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
                    String.valueOf(newComment.getId()) // id (ép kiểu về String cho khớp DTO)
            ));

        } catch (Exception e) {
            // Trả về lỗi nếu có vấn đề (sai ID bài viết, lỗi database...)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
