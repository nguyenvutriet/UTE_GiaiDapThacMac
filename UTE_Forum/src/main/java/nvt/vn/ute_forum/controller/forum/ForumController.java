package nvt.vn.ute_forum.controller.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.model.decorator.BadgeDecoratorFactory;
import nvt.vn.ute_forum.model.strategy.forum.ForumSortContext;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.VoteService;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo;
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

    @Autowired private RequestService requestService;
    @Autowired private UsersRepo usersRepo;
    @Autowired private nvt.vn.ute_forum.repository.CategoryRepo categoryRepo;
    @Autowired private nvt.vn.ute_forum.repository.DepartmentRepo departmentRepo;
    @Autowired private VoteService voteService;
    @Autowired private ForumSortContext forumSortContext;       // Strategy
    @Autowired private BadgeDecoratorFactory badgeDecoratorFactory; // Decorator

    // ------------------------------------------------------------------
    // TRANG CHÍNH DIỄN ĐÀN
    // ------------------------------------------------------------------

    @GetMapping("/view")
    public String showForumPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "newest") String sortBy,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());

        String currentUserId = resolveCurrentUserId(userDetails, model);

        Page<ForumPostDTO> postPage = requestService.getPublicPosts(pageable, currentUserId);

        // Strategy: sắp xếp lại nội dung trang hiện tại
        List<ForumPostDTO> sortedContent = forumSortContext.sort(sortBy, postPage.getContent());

        // Decorator: gắn badge Hot / Trending
        List<ForumPostDTO> decorated = badgeDecoratorFactory.decorateAll(sortedContent);

        model.addAttribute("requests", decorated);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("currentSort", sortBy);

        // Truyền danh sách sort options từ Context để render <select> tự động
        model.addAttribute("sortOptions", forumSortContext.getAvailableOptions());

        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());

        return "student/forumView";
    }

    // ------------------------------------------------------------------
    // CHI TIẾT BÀI VIẾT
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable String id,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserId = resolveCurrentUserId(userDetails, model);

        ForumPostDTO post = requestService.getPostDetail(id, currentUserId);
        if (post == null) return "redirect:/forum/view";

        // Decorator: áp dụng badge cho bài viết đơn lẻ
        ForumPostDTO decorated = badgeDecoratorFactory.decorate(post);
        model.addAttribute("post", decorated);

        return "student/postDetail";
    }

    // ------------------------------------------------------------------
    // REACT (LIKE/LOVE...) — gọi Observer bên trong RequestService
    // ------------------------------------------------------------------

    @PostMapping("/react")
    @ResponseBody
    public ResponseEntity<?> reactPost(
            @RequestParam String postId,
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập!");

        Users user = usersRepo.findByEmail(userDetails.getUsername());
        if (user == null) return ResponseEntity.status(404).body("Không tìm thấy user");

        try {
            ReactionType reactionType = ReactionType.valueOf(type.toUpperCase());
            // votePost() bên trong RequestService sẽ gọi forumEventPublisher.checkReactionMilestone()
            Map<String, Object> result = requestService.votePost(postId, user.getId(), reactionType);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Loại reaction không hợp lệ!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // FILTER + SORT (AJAX fragment) — tích hợp đầy đủ Strategy+Decorator
    // ------------------------------------------------------------------

    @GetMapping("/filter")
    public String filterPosts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String currentUserId = resolveCurrentUserId(userDetails, model);

        // getFilteredPosts đã tích hợp Strategy + Decorator bên trong
        List<ForumPostDTO> posts = requestService.getFilteredPosts(
                categoryId, departmentId, sortBy, currentUserId);

        model.addAttribute("requests", posts);
        return "student/forumView :: .post-list";
    }

    // ------------------------------------------------------------------
    // API: DANH SÁCH SORT OPTIONS (cho JS render <select> động nếu cần)
    // ------------------------------------------------------------------

    @GetMapping("/sort-options")
    @ResponseBody
    public List<ForumSortContext.SortOption> getSortOptions() {
        return forumSortContext.getAvailableOptions();
    }

    // ------------------------------------------------------------------
    // TÌM KIẾM
    // ------------------------------------------------------------------

    @GetMapping("/search")
    @ResponseBody
    public List<ForumPostDTO> searchPosts(@RequestParam("keyword") String keyword) {
        return requestService.searchPosts(keyword);
    }

    @GetMapping("/search-list")
    public String searchPublicPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "newest") String sortBy,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());
        String currentUserId = resolveCurrentUserId(userDetails, model);

        Page<ForumPostDTO> postPage = requestService.getPublicSearchPosts(
                keyword.trim(), pageable, currentUserId);

        // Strategy + Decorator áp dụng trên kết quả tìm kiếm
        List<ForumPostDTO> sorted    = forumSortContext.sort(sortBy, postPage.getContent());
        List<ForumPostDTO> decorated = badgeDecoratorFactory.decorateAll(sorted);

        model.addAttribute("requests", decorated);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("sortOptions", forumSortContext.getAvailableOptions());
        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());

        return "student/forumView";
    }

    // ------------------------------------------------------------------
    // CHI TIẾT NGƯỜI THẢ TIM BÀI VIẾT
    // ------------------------------------------------------------------

//    @GetMapping("/reactors/details")
//    @ResponseBody
//    public ResponseEntity<?> getPostReactionDetails(
//            @RequestParam String postId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập!");
//        return ResponseEntity.ok(voteService.getReactionDetails(postId));
//    }

    // ------------------------------------------------------------------
    // HELPER
    // ------------------------------------------------------------------

    private String resolveCurrentUserId(UserDetails userDetails, Model model) {
        if (userDetails == null) return null;
        Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
        if (currentUser == null) return null;
        model.addAttribute("user", currentUser);
        return currentUser.getId();
    }
}