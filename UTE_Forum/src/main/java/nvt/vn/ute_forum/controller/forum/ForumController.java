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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo; // Inject Repo để tìm User từ DB
    // 🔥 1. Inject thêm 2 Repo này để lấy dữ liệu cho ô Select
    @Autowired
    private nvt.vn.ute_forum.repository.CategoryRepo categoryRepo;
    @Autowired
    private nvt.vn.ute_forum.repository.DepartmentRepo departmentRepo;

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
        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());
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

    @GetMapping("/post/{postId}")
    @ResponseBody
    public ResponseEntity<ForumPostDTO> getPostById(@PathVariable String postId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }
        }

        Optional<ForumPostDTO> post = requestService.getPublicPostById(postId, currentUserId);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public String filterPosts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) currentUserId = currentUser.getId();
        }

        // Lấy dữ liệu đã lọc
        List<ForumPostDTO> posts = requestService.getFilteredPosts(categoryId, departmentId, sortBy, currentUserId);

        model.addAttribute("requests", posts); // Đè dữ liệu mới vào biến 'requests'

        // Trả về fragment "post-list" trong file "student/forumView"
        return "student/forumView :: .post-list";
    }
}