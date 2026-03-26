package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.dto.ReactionDetailDTO;
import nvt.vn.ute_forum.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @GetMapping("/reactors/details")
    public ResponseEntity<?> getDetailedReactors(
            @RequestParam String postId,
            @AuthenticationPrincipal UserDetails userDetails) { // Thêm cái này vô má ơi

        // 1. Chặn ngay từ đầu nếu chưa login
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Đăng nhập mới coi được ai thả tim nha má!");
        }

        // 2. Kiểm tra postId có bị trống hông (tránh lỗi 400 Bad Request)
        if (postId == null || postId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Thiếu postId rồi má ơi!");
        }

        try {
            // Gọi service lấy DTO như cũ của má
            List<ReactionDetailDTO> details = voteService.getReactionDetails(postId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server rồi má!");
        }
    }

}