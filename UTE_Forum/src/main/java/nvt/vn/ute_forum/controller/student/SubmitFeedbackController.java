package nvt.vn.ute_forum.controller.student;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SubmitFeedbackController {

    @Autowired
    private RequestService requestService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UsersService usersService;

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
                               @RequestParam(value = "privacy", required = false) String privacy,
                               @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
                               Authentication authentication,
                               Model model) {
        try {
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }

            requestService.submitStudentFeedback(
                    subject,
                    description,
                    categoryIds,
                    departmentId,
                    privacy,
                    attachments,
                    user
            );

            return "redirect:/api/history";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            populateFormData(model, user);
            return "student/submit-feedback";
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
                                 @RequestParam(value = "privacy", required = false) String privacy,
                                 @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
                                 Authentication authentication,
                                 Model model) {
        try {
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }

            requestService.updateStudentFeedback(
                    requestId,
                    subject,
                    description,
                    categoryIds,
                    departmentId,
                    privacy,
                    attachments,
                    user.getId()
            );
            return "redirect:/api/history";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            Users user = resolveAuthenticatedUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            Request request = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
            model.addAttribute("request", request);
            model.addAttribute("selectedCategoryIds", request == null ? java.util.Collections.emptyList() : request.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
            populateFormData(model, user);
            return "student/edit-feedback";
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

}