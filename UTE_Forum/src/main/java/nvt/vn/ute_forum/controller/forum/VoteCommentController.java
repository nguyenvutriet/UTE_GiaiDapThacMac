package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.service.VoteCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/vote-comment")
public class VoteCommentController {

    @Autowired
    private VoteCommentService voteCommentService;

    // Sửa Post thành Get ở đây
    @GetMapping("/{commentId}")
    public ResponseEntity<?> react(@PathVariable String commentId,
                                   @RequestParam("type") ReactionType type, // Thêm ("type") cho chắc
                                   Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập!");
        }

        String userId = principal.getName();
        return ResponseEntity.ok(voteCommentService.toggleReaction(userId, commentId, type));
    }
    @GetMapping("/detail/{commentId}")
    public ResponseEntity<?> getReactionDetail(@PathVariable String commentId) {
        return ResponseEntity.ok(voteCommentService.getReactionUsers(commentId));
    }
}