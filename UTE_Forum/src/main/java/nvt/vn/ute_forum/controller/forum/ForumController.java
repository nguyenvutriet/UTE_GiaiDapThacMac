package nvt.vn.ute_forum.controller.forum;


import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.model.Request;
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

        // 2. Gọi hàm convertToFullDTO thần thánh của bà
        ForumPostDTO post = requestService.getPostDetail(id, currentUserId);

        if (post == null) {
            return "redirect:/forum/view"; // Không thấy bài thì cho về vườn
        }

        // 3. Đẩy dữ liệu ra trang chi tiết
        model.addAttribute("post", post);

        return "student/postDetail"; // Đường dẫn tới file HTML chi tiết của bà
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
}