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
    private UsersRepo usersRepo;
    @Autowired
    private nvt.vn.ute_forum.repository.CategoryRepo categoryRepo;
    @Autowired
    private nvt.vn.ute_forum.repository.DepartmentRepo departmentRepo;

    @GetMapping("/view")
    public String showForumPage(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Phân trang: 10 bài mỗi trang, sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());

        // Lấy ID user hiện tại nếu đăng nhập
        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("user", currentUser); // Thêm user vào Model
            }
        }

        // Lấy bài viết từ service
        Page<ForumPostDTO> postPage = requestService.getPublicPosts(pageable, currentUserId);

        // 4. Đưa vào Model
        model.addAttribute("requests", postPage.getContent()); // Danh sách bài
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());
        return "student/forumView";
    }



    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable String id,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Lấy user hiện tại để check reaction
        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("user", currentUser);
            }
        }

        // 2. Gọi hàm convertToFullDTO
        ForumPostDTO post = requestService.getPostDetail(id, currentUserId);

        if (post == null) {
            return "redirect:/forum/view";
        }

        model.addAttribute("post", post);

        return "student/postDetail";
    }

    @PostMapping("/react")
    @ResponseBody
    public ResponseEntity<?> reactPost( // Đổi sang ResponseEntity cho chuyên nghiệp má ơi
                                        @RequestParam String postId,
                                        @RequestParam String type, // Nhận String rồi ép kiểu trong Service cho an toàn, tránh lỗi 400
                                        @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập má ơi!");
        }

        Users user = usersRepo.findByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy user");
        }

        try {
            // Ép kiểu String sang Enum ReactionType
            ReactionType reactionType = ReactionType.valueOf(type.toUpperCase());

            // Gọi Service và nhận về một Object bọc cả (Counts + CurrentType)
            Map<String, Object> result = requestService.votePost(postId, user.getId(), reactionType);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại reaction không hợp lệ!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi rồi: " + e.getMessage());
        }
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

    @GetMapping("/search")
    @ResponseBody
    public List<ForumPostDTO> searchPosts(@RequestParam("keyword") String keyword) {
        return requestService.searchPosts(keyword); // em implement rồi
    }

    @GetMapping("/search-list")
    public String searchPublicPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Phân trang tương tự như trang chủ Forum
        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());

        // 2. Lấy ID user hiện tại để kiểm tra xem họ đã "Like/Love" bài đó chưa
        String currentUserId = null;
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("user", currentUser);
            }
        }

        // 3. Gọi hàm search mới (đã bao gồm mapping Reactions/Comments giống getPublicPosts)
        // Lưu ý: keyword cần được trim để tránh khoảng trắng thừa
        Page<ForumPostDTO> postPage = requestService.getPublicSearchPosts(keyword.trim(), pageable, currentUserId);

        // 4. Đưa dữ liệu vào Model (giữ nguyên tên biến để dùng chung Template HTML)
        model.addAttribute("requests", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("keyword", keyword); // Gửi lại keyword để hiển thị trên ô Search hoặc dùng cho phân trang

        // Các dữ liệu bổ trợ cho Sidebar/Filter
        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());

        return "student/forumView"; // Dùng chung giao diện với trang Forum chính
    }
}