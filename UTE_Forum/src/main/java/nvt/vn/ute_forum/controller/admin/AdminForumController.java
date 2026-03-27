package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.UsersService;
import nvt.vn.ute_forum.service.CategoryService;
import nvt.vn.ute_forum.service.DepartmentService;

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

import java.security.Principal;

@Controller
@RequestMapping("/admin/forum")
public class AdminForumController {

    private final RequestService requestService;
    private final UsersService usersService;
    private final CategoryService categoryService;
    private final DepartmentService departmentService;
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private nvt.vn.ute_forum.repository.CategoryRepo categoryRepo;
    @Autowired
    private nvt.vn.ute_forum.repository.DepartmentRepo departmentRepo;

    public AdminForumController(
            RequestService requestService,
            UsersService usersService,
            CategoryService categoryService,
            DepartmentService departmentService
    ) {
        this.requestService = requestService;
        this.usersService = usersService;
        this.categoryService = categoryService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String adminForum(
            Model model,
            Principal principal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String categoryId,
            @RequestParam(value = "department", required = false) String departmentId,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam(value = "commentId", required = false) String commentId
    ) {

        // 1) Lấy admin đăng nhập
        Users admin = usersService.getByEmail(principal.getName());
        model.addAttribute("user", admin);

        // 2) Phân trang
        PageRequest pageable = PageRequest.of(page, 10);

        // 3) Lấy dữ liệu forum
        Page<?> data;

        boolean hasFilter =
                (keyword != null && !keyword.isEmpty())
                        || (categoryId != null && !categoryId.isEmpty())
                        || (departmentId != null && !departmentId.isEmpty());

        if (hasFilter) {
            // Nếu có lọc → dùng getFilteredPosts() (không phân trang)
            var list = requestService.getFilteredPosts(categoryId, departmentId, "newest", admin.getId());

            // Tạo trang thủ công
            int start = page * 10;
            int end = Math.min(start + 10, list.size());
            var subList = list.subList(start, end);

            data = new org.springframework.data.domain.PageImpl<>(
                    subList,
                    pageable,
                    list.size()
            );
        } else {
            // Không lọc → lấy bài PUBLIC
            data = requestService.getPublicPosts(pageable, admin.getId());
        }

        // 4) Truyền vào HTML
        model.addAttribute("requests", data.getContent());
        model.addAttribute("currentPage", data.getNumber());
        model.addAttribute("totalPages", data.getTotalPages());

        // 5) Trả lại các filter để HTML giữ trạng thái
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedDepartment", departmentId);

        model.addAttribute("allCategories", categoryService.getAllCategories());
        model.addAttribute("allDepartments", departmentService.getAllDepartments());

        // 6) Deep-link để mở modal + highlight comment
        model.addAttribute("deepLinkRequestId", requestId);
        model.addAttribute("deepLinkCommentId", commentId);

        return "admin/admin-forum";
    }

    @GetMapping("/view")
    public String showForumPagee(
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
        return "admin/admin-forum";
    }
}