package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.service.VoteCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
                                   @RequestParam("type") ReactionType type,
                                   @AuthenticationPrincipal UserDetails userDetails) { // Sửa ở đây nè má!

        // 1. Kiểm tra đăng nhập (Bảo mật cốt lõi)
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập mới được thả tim nha má!");
        }

        // 2. Lấy Email/Username từ userDetails (getUsername trả về email trong UserPrincipal của má)
        String userEmail = userDetails.getUsername();

        // 3. Gọi service (Vẫn giữ nguyên logic toggle của má)
        return ResponseEntity.ok(voteCommentService.toggleReaction(userEmail, commentId, type));
    }
    // Cách 1: Đổi cả hai thành commentId cho đồng bộ
    @GetMapping("/detail/{commentId}")
    public ResponseEntity<?> getReactionDetail(
            @PathVariable String commentId, // Phải trùng tên với {commentId} ở trên
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(voteCommentService.getReactionUsers(commentId));
    }
}