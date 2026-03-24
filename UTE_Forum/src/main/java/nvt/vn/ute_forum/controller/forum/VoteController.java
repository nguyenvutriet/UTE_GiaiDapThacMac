package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.dto.ReactionDetailDTO;
import nvt.vn.ute_forum.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // Chỉ cần duy nhất Endpoint này để lấy dữ liệu cho Modal
    @GetMapping("/reactors/details")
    public ResponseEntity<List<ReactionDetailDTO>> getDetailedReactors(@RequestParam String postId) {
        // Gọi service để lấy List các DTO (chứa Fullname, UserID, Type)
        List<ReactionDetailDTO> details = voteService.getReactionDetails(postId);
        return ResponseEntity.ok(details);
    }
}