package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.service.*;
import nvt.vn.ute_forum.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private ClarificationConversationService clarificationConversationService;

    @Autowired
    private RequestStatusHistoryService requestStatusHistoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/list-feedbacks")
    public String getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 🔥 1. Sort
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 12, sort);

        // 🔥 2. Lấy user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // 🔥 3. Gọi service (role xử lý bên service)
        Page<Request> requestPage = requestService.getAllFeedbacks(pageable, user);

        // 🔥 4. Đẩy data ra view
        model.addAttribute("feedbacks", requestPage.getContent());
        List<Category> categories =
                categoryService.getCategoriesByDepartment(user);

        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentUser", user);

        return "staff/staff-list";
    }

    @GetMapping("/feedback-detail")
    public String getFeedbackDetail(
            @RequestParam("id") String id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 🔥 lấy user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // 🔥 truyền user vào service
        Request request = requestService.getFeedbackDetail(id, user);

        ClarificationConversation conversation = clarificationConversationService.getClarificationConversation(request.getId());

        model.addAttribute("conversation", conversation);
        model.addAttribute("feedback", request);
        model.addAttribute("currentUser", user);
        model.addAttribute("forwardLogs", request.getForwardingLogs());
        model.addAttribute("histories",
                requestStatusHistoryService.getByRequestId(request.getId()));
        model.addAttribute("departments", departmentRepo.findAll());
        return "staff/feedback-detail";
    }

    @GetMapping("/search-feedbacks")
    public String searchFeedbacks(
            @RequestParam("keyword") String keyword,
            @RequestParam(required = false) String categoryId, // 🔥 thêm
            @RequestParam(required = false) String status,     // 🔥 thêm
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // sort
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 12, sort);

        // user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // gọi service
        Page<Request> resultPage = requestService.searchFeedbacks(keyword, pageable, user);

        // 🔥 alt flow
        if (resultPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy");
        }

        // data
        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", user);

        // 🔥 GIỮ STATE (THÊM 3 DÒNG NÀY LÀ ĐỦ)
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("categories",
                categoryService.getCategoriesByDepartment(user));

        return "staff/staff-list";
    }

    @GetMapping("/filter-feedbacks")
    public String filterFeedbacks(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 🔥 lấy user
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // 🔥 xử lý ALL + fix lambda (tạo biến final)
        String finalCategoryId = "ALL".equals(categoryId) ? null : categoryId;
        String finalStatus = "ALL".equals(status) ? null : status;

        // 🔥 map status -> name
        Map<String, String> statusMap = Map.of(
                "PENDING", "Đang chờ tiếp nhận",
                "APPROVED", "Đang xử lý",
                "RESOLVED", "Đã xử lý",
                "FORWARDING", "Chuyển tiếp",
                "REJECTED", "Từ chối"
        );

        //  giữ trạng thái selected
        model.addAttribute("selectedStatus", finalStatus);
        model.addAttribute("selectedCategory", finalCategoryId);

        //  tên status (fix nút bị reset)
        model.addAttribute("selectedStatusName",
                finalStatus != null ? statusMap.get(finalStatus) : null);

        //  tên category (fix lambda error luôn)
        String categoryName = null;
        if (finalCategoryId != null) {
            categoryName = categoryService.getCategoriesByDepartment(user).stream()
                    .filter(c -> c.getId().equals(finalCategoryId))
                    .map(Category::getSubject)
                    .findFirst()
                    .orElse(null);
        }
        model.addAttribute("selectedCategoryName", categoryName);

        // 🔥 paging
        Pageable pageable = PageRequest.of(page, 12);

        // 🔥 gọi service
        Page<Request> resultPage =
                requestService.getFeedbacks(finalCategoryId, finalStatus, pageable, user);

        // 🔥 data ra view
        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());

        model.addAttribute("categories",
                categoryService.getCategoriesByDepartment(user));

        model.addAttribute("statuses",
                requestService.getAvailableStatuses(user));

        model.addAttribute("currentUser", user);

        return "staff/staff-list";
    }

    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(
            @RequestParam String requestId,
            @RequestParam String status
    ) {

        requestService.updateStatus(requestId, status);

        //  lấy lại request
        Request request = requestRepo.findById(requestId).orElseThrow();

        //  tạo notification
        notificationService.createStatusNotification(request, status);

        publishRequestStatusSyncEvent(requestId, status, "STATUS_UPDATED");


        return ResponseEntity.ok("Updated");
    }

    @PostMapping("/forward")
    @ResponseBody
    public ResponseEntity<?> forward(
            @RequestParam String requestId,
            @RequestParam String toDeptId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // user hiện tại
        Users staffUser = usersRepo.findByEmail(userDetails.getUsername());

        // xử lý forward
        requestService.forwardRequest(requestId, toDeptId, note, staffUser);

        // lấy lại request
        Request request = requestRepo.findById(requestId).orElseThrow();

        // phòng hiện tại
        Department currentDept = staffUser.getDepartment();

        // phòng được chuyển đến
        Department toDept = departmentRepo.findById(toDeptId).orElseThrow();

        // tạo notification
        notificationService.createForwardNotifications(
                request,
                currentDept,
                toDept,
                staffUser
        );

        publishRequestStatusSyncEvent(requestId, "FORWARDING", "FORWARDED");

        return ResponseEntity.ok("Forward success");
    }

    private void publishRequestStatusSyncEvent(String requestId, String status, String eventType) {
        if (requestId == null || requestId.isBlank()) {
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("requestId", requestId);
        payload.put("status", status == null ? "" : status);
        payload.put("eventType", eventType == null ? "STATUS_UPDATED" : eventType);

        messagingTemplate.convertAndSend("/topic/request-status/" + requestId, payload);
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        // 1. Kiểm tra nếu chưa đăng nhập
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 2. Lấy email từ currentUser (getUsername trong class của bạn trả về email)
        String email = currentUser.getUsername();

        // 3. Truy vấn DB để lấy Object Users có kèm Department
        Users user = usersRepo.findByEmail(email);

        if (user != null && user.getDepartment() != null) {
            // Đổ các thông tin cần thiết ra để HTML không bị Null
            model.addAttribute("user", user);
            model.addAttribute("deptId", user.getDepartment().getId());
// Sửa dòng này
            model.addAttribute("deptName", user.getDepartment().getName());
        } else {
            // Trường hợp user không có phòng ban, tránh lỗi sập trang
            model.addAttribute("deptName", "N/A");
        }

        return "staff/dashboard";
    }

}