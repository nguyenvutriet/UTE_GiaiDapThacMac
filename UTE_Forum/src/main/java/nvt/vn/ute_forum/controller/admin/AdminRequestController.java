package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminRequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClarificationConversationService clarificationConversationService;

    // ===================================================================
    // 1. DANH SÁCH TẤT CẢ GÓP Ý (admin thấy hết, kể cả ẩn danh)
    // ===================================================================
    @GetMapping("/list-feedbacks")
    public String getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Sort
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 15, sort);

        Users admin = usersRepo.findByEmail(userDetails.getUsername());

        Page<Request> requestPage = requestService.getAllFeedbacks(pageable, admin);

        List<Category> categories = categoryRepo.findAll();
        List<Department> departments = departmentRepo.findAll();

        model.addAttribute("feedbacks", requestPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("departments", departments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("totalElements", requestPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentUser", admin);

        return "admin/admin-list";
    }

    // ===================================================================
    // 2. CHI TIẾT GÓP Ý
    // ===================================================================
    @GetMapping("/feedback-detail")
    public String getFeedbackDetail(
            @RequestParam("id") String id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Users admin = usersRepo.findByEmail(userDetails.getUsername());

        // Admin xem được tất cả, không check quyền phòng ban
        Request request = requestService.getAdminFeedbackDetail(id);

        var conversation = clarificationConversationService.getClarificationConversation(request.getId());

        model.addAttribute("conversation", conversation);
        model.addAttribute("feedback", request);
        model.addAttribute("currentUser", admin);
        model.addAttribute("forwardLogs", request.getForwardingLogs());

        return "admin/feedback-detail";
    }

    // ===================================================================
    // 3. TÌM KIẾM GÓP Ý
    // ===================================================================
    @GetMapping("/search-feedbacks")
    public String searchFeedbacks(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 15, sort);

        Users admin = usersRepo.findByEmail(userDetails.getUsername());

        // Admin search tất cả (dùng hàm không lọc phòng ban)
        Page<Request> resultPage = requestService.searchAllFeedbacks(keyword, pageable);

        if (resultPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy góp ý nào phù hợp.");
        }

        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("totalElements", resultPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentUser", admin);

        // Vẫn cần list để filter không bị mất
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("departments", departmentRepo.findAll());

        return "admin/admin-list";
    }

    // ===================================================================
    // 4. LỌC GÓP Ý (theo phòng ban + trạng thái + danh mục)
    // ===================================================================
    @GetMapping("/filter-feedbacks")
    public String filterFeedbacks(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Users admin = usersRepo.findByEmail(userDetails.getUsername());

        String finalDeptId = "ALL".equals(departmentId) ? null : departmentId;
        String finalCategoryId = "ALL".equals(categoryId) ? null : categoryId;
        String finalStatus = "ALL".equals(status) ? null : status;

        Map<String, String> statusMap = Map.of(
                "PENDING", "Đang chờ tiếp nhận",
                "APPROVED", "Đang xử lý",
                "RESOLVED", "Đã xử lý",
                "FORWARDING", "Chuyển tiếp",
                "REJECTED", "Từ chối"
        );

        model.addAttribute("selectedDepartment", finalDeptId);
        model.addAttribute("selectedStatus", finalStatus);
        model.addAttribute("selectedCategory", finalCategoryId);

        model.addAttribute("selectedStatusName",
                finalStatus != null ? statusMap.get(finalStatus) : null);

        String deptName = null;
        if (finalDeptId != null) {
            deptName = departmentRepo.findById(finalDeptId)
                    .map(Department::getName)
                    .orElse(null);
        }
        model.addAttribute("selectedDepartmentName", deptName);

        String catName = null;
        if (finalCategoryId != null) {
            catName = categoryRepo.findById(finalCategoryId)
                    .map(Category::getSubject)
                    .orElse(null);
        }
        model.addAttribute("selectedCategoryName", catName);

        Pageable pageable = PageRequest.of(page, 15);

        Page<Request> resultPage =
                requestService.getAdminFeedbacks(finalDeptId, finalCategoryId, finalStatus, pageable);

        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("totalElements", resultPage.getTotalElements());

        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("departments", departmentRepo.findAll());
        model.addAttribute("currentUser", admin);

        return "admin/admin-list";
    }
}