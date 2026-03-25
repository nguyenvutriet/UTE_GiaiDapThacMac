package nvt.vn.ute_forum.controller.forum;


import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo; // Import Repo của bạn
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo; // Inject Repo để tìm User từ DB

    @GetMapping("/view")
    public String showForumPage(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Phân trang: 10 bài mỗi trang, sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());

        // 2. Lấy ID user hiện tại nếu đăng nhập
        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("user", currentUser); // Thêm user vào Model
            }
        }

        // 3. Lấy bài viết từ service mới
        Page<ForumPostDTO> postPage = requestService.getPublicPosts(pageable, currentUserId);

        // 4. Đưa vào Model
        model.addAttribute("requests", postPage.getContent()); // Danh sách bài
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        return "student/forumView";
    }

    @PostMapping("/react")
    @ResponseBody
    public Map<String, Long> reactPost(
            @RequestParam String postId,
            @RequestParam ReactionType type,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) throw new RuntimeException("Not authenticated");

        Users user = usersRepo.findByEmail(userDetails.getUsername());
        if (user == null) throw new RuntimeException("User not found");

        return requestService.votePost(postId, user.getId(), type);
    }
}