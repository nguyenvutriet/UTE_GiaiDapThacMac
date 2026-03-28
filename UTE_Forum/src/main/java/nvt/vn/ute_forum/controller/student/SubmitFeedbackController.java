package nvt.vn.ute_forum.controller.student;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.NotificationRepo;
import nvt.vn.ute_forum.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class SubmitFeedbackController {

    private static final String DEFAULT_DEPARTMENT_ID = "DEP_CTSV";

    private final RequestService requestService;
    private final CategoryService categoryService;
    private final DepartmentService departmentService;
    private final RequestStatusHistoryService requestStatusHistoryService;
    private final FileAttachmentService fileAttachmentService;
    private final UsersService usersService;
    private final IdGeneratorService idGeneratorService;
    private final NotificationRepo notificationRepo;

    public SubmitFeedbackController(RequestService requestService,
                                    CategoryService categoryService,
                                    DepartmentService departmentService,
                                    RequestStatusHistoryService requestStatusHistoryService,
                                    FileAttachmentService fileAttachmentService,
                                    UsersService usersService,
                                    IdGeneratorService idGeneratorService,
                                    NotificationRepo notificationRepo) {
        this.requestService = requestService;
        this.categoryService = categoryService;
        this.departmentService = departmentService;
        this.requestStatusHistoryService = requestStatusHistoryService;
        this.fileAttachmentService = fileAttachmentService;
        this.usersService = usersService;
        this.idGeneratorService = idGeneratorService;
        this.notificationRepo = notificationRepo;
    }

    @GetMapping("/api/submit")
    public String showSendFeedback(Authentication authentication, Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        populateFormData(model, user);
        return "student/submit-feedback";
    }

    @GetMapping("/api/edit-request")
    public String editFeedback(@RequestParam("id") String id, Authentication authentication, Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        Request request = requestService.getRequestByIdAndUserId(id, user.getId()).orElse(null);
        if (request == null || !request.getCurrentStatus().equals("PENDING")) {
            return "redirect:/api/history";
        }
        
        populateFormData(model, user);
        model.addAttribute("request", request);
        model.addAttribute("selectedCategoryIds", request.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
        return "student/edit-feedback";
    }

    @PostMapping("/api/delete-request")
    public String deleteFeedback(@RequestParam("id") String id, Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user != null) {
            Request request = requestService.getRequestByIdAndUserId(id, user.getId()).orElse(null);
            if (request != null && request.getCurrentStatus().equals("PENDING")) {
                requestService.deleteRequest(id, user.getId());
            }
        }
        return "redirect:/api/history";
    }

    @PostMapping({"/api/submit", "/feedback/send"})
    public String sendFeedback(@RequestParam(value = "subject") String subject,
                               @RequestParam(value = "description") String description,
                               @RequestParam(value = "categories", required = false) List<String> categoryIds,
                               @RequestParam(value = "department", required = false) String departmentId,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "privacy", required = false) String privacy,
                               @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
                               Authentication authentication,
                               Model model) {
        try {
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }

            if (!isValidPrivacyMode(privacy)) {
                model.addAttribute("error", "Vui lòng chọn chế độ gửi: Công khai hoặc Gửi đến phòng ban.");
                populateFormData(model, user);
                return "student/submit-feedback";
            }

            Department department = resolveTargetDepartment(departmentId).orElse(null);

            if (department == null) {
                model.addAttribute("error", "Phòng ban không tồn tại (mặc định: DEP_CTSV).");
                populateFormData(model, user);
                return "student/submit-feedback";
            }

            Request request = new Request();
            request.setId(idGeneratorService.nextRequestId());
            request.setCurrentStatus("PENDING");
            request.setTimeCreate(LocalDateTime.now());
            request.setUser(user);

            request.setSubject(subject);
            request.setDescription(description);
            request.setPostStatus("public".equals(privacy) ? "PUBLIC" : "PRIVATE");
            request.setDepartment(department);

            request.getCategories().clear();
            request.getCategories().addAll(resolveCategories(categoryIds));

            Request savedRequest = requestService.saveOrUpdate(request);
            requestStatusHistoryService.createInitialStatus(savedRequest, savedRequest.getCurrentStatus());
            
            if (hasAnyAttachment(attachments)) {
                fileAttachmentService.saveRequestAttachments(savedRequest, attachments);
            }
            
            // Notify department staff
            java.util.List<Users> deptStaffs = department.getUsers().stream()
                    .filter(u -> "ROLE_DEPARTMENT".equals(u.getRole()))
                    .collect(java.util.stream.Collectors.toList());
                    
            if (!deptStaffs.isEmpty()) {
                Notification notification = new Notification();
                notification.setId(idGeneratorService.nextNotificationId());
                notification.setNotificationType("NEW_FEEDBACK_RECEIVED");
                notification.setTitle("Góp ý mới gửi đến phòng ban");
                notification.setContent("Góp ý: " + savedRequest.getSubject());
                notification.setRead(false);
                notification.setCreateAt(java.time.LocalDateTime.now());
                notification.setUsers(deptStaffs);
                notificationRepo.save(notification);
            }

            return "redirect:/api/history";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi gửi góp ý: " + e.getMessage());
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            populateFormData(model, user);
            return "student/submit-feedback";
        }
    }

    @PostMapping("/api/update-request")
    public String updateFeedback(@RequestParam(value = "request_id") String requestId,
                                 @RequestParam(value = "subject") String subject,
                                 @RequestParam(value = "description") String description,
                                 @RequestParam(value = "categories", required = false) List<String> categoryIds,
                                 @RequestParam(value = "department", required = false) String departmentId,
                                 @RequestParam(value = "location", required = false) String location,
                                 @RequestParam(value = "privacy", required = false) String privacy,
                                 @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
                                 Authentication authentication,
                                 Model model) {
        try {
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }

            if (!isValidPrivacyMode(privacy)) {
                model.addAttribute("error", "Vui lòng chọn chế độ gửi: Công khai hoặc Gửi đến phòng ban.");
                Request existingRequest = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
                model.addAttribute("request", existingRequest);
                model.addAttribute("selectedCategoryIds", existingRequest == null ? java.util.Collections.emptyList() : existingRequest.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
                populateFormData(model, user);
                return "student/edit-feedback";
            }

            Department department = resolveTargetDepartment(departmentId).orElse(null);

            if (department == null) {
                model.addAttribute("error", "Phòng ban không tồn tại (mặc định: DEP_CTSV).");
                Request existingRequest = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
                model.addAttribute("request", existingRequest);
                model.addAttribute("selectedCategoryIds", existingRequest == null ? java.util.Collections.emptyList() : existingRequest.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
                populateFormData(model, user);
                return "student/edit-feedback"; // Return to edit feedback if error
            }

            if (requestId == null || requestId.isBlank()) {
                return "redirect:/api/history";
            }

            Request request = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
            if (request == null || !request.getCurrentStatus().equals("PENDING")) {
                return "redirect:/api/history";
            }
            
            request.getCategories().clear();
            request.setSubject(subject);
            request.setDescription(description);
            request.setPostStatus("public".equals(privacy) ? "PUBLIC" : "PRIVATE");
            request.setDepartment(department);

            request.getCategories().addAll(resolveCategories(categoryIds));

            Request savedRequest = requestService.saveOrUpdate(request);
            if (hasAnyAttachment(attachments)) {
                fileAttachmentService.saveRequestAttachments(savedRequest, attachments);
            }
            return "redirect:/api/history";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi sửa góp ý: " + e.getMessage());
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            
            Request request = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
            model.addAttribute("request", request);
            model.addAttribute("selectedCategoryIds", request == null ? java.util.Collections.emptyList() : request.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
            populateFormData(model, user);
            return "student/edit-feedback";
        }
    }

    private void populateFormData(Model model, Users user) {
        model.addAttribute("user", user);
        model.addAttribute("roleLabel", "Sinh vien");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("departments", departmentService.getAllDepartments());
    }

    private Users resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof UserPrincipal userPrincipal) {
            email = userPrincipal.getUsername();
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalName) {
            email = principalName;
        }

        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return null;
        }

        return usersService.getByEmail(email.trim());
    }

    private boolean hasAnyAttachment(MultipartFile[] attachments) {
        if (attachments == null || attachments.length == 0) {
            return false;
        }
        for (MultipartFile attachment : attachments) {
            if (attachment != null && !attachment.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPrivacyMode(String privacy) {
        return "public".equals(privacy) || "department".equals(privacy);
    }

    private java.util.Optional<Department> resolveTargetDepartment(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            return departmentService.getDepartmentById(DEFAULT_DEPARTMENT_ID);
        }
        java.util.Optional<Department> foundDepartment = departmentService.getDepartmentById(departmentId);
        if (foundDepartment.isPresent()) {
            return foundDepartment;
        }
        return departmentService.getDepartmentById(DEFAULT_DEPARTMENT_ID);
    }

    private List<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<String> cleanedIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (cleanedIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return categoryService.getAllCategories().stream()
                .filter(category -> cleanedIds.contains(category.getId()))
                .collect(Collectors.toList());
    }
}

