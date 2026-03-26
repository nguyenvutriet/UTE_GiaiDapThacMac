package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.service.CommentService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments/admin")
public class AdminCommentRestController {

    private final CommentService commentService;
    private final UsersService usersService;

    public AdminCommentRestController(CommentService commentService,
                                      UsersService usersService) {
        this.commentService = commentService;
        this.usersService = usersService;
    }

    /** ẨN COMMENT */
    @PostMapping("/hide/{commentId}")
    public ResponseEntity<?> hideComment(@PathVariable String commentId,
                                         Principal principal) {

        Comment c = commentService.getCommentById(commentId);
        if (c == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy comment");
        }

        // Admin name (nếu cần log)
        String adminEmail = principal.getName();
        var admin = usersService.getByEmail(adminEmail);

        c.setIsActive(false); // Ẩn comment
        commentService.save(c);

        return ResponseEntity.ok("Đã ẩn bình luận");
    }
}