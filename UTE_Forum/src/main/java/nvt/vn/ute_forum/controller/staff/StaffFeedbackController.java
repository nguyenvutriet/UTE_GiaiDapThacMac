package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.dto.StaffAnnouncementCardDTO;
import nvt.vn.ute_forum.service.AnnoucementService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/staff")
public class StaffFeedbackController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private AnnoucementService annoucementService;

    @GetMapping("/list-feedbacks")
    public String viewAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Phân trang 12 dòng mỗi trang cho giao diện Staff
        Pageable pageable = PageRequest.of(page, 12, Sort.by("timeCreate").descending());

        // Lấy ID user hiện tại để pass vào service
        String currentUserId = null;
        if (userDetails != null) {
            Users staff = usersRepo.findByEmail(userDetails.getUsername());
            if (staff != null) {
                currentUserId = staff.getId();
                model.addAttribute("staff", staff);
            }
        }

        // Gọi service mới, có truyền currentUserId
        Page<ForumPostDTO> feedbackPage = requestService.getPublicPosts(pageable, currentUserId);

        model.addAttribute("feedbacks", feedbackPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());

        // Trả về file nằm trong: templates/staff/feedback_list.html
        return "staff/staff-list";
    }

    @GetMapping("/forum")
    public String viewStaffForum(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "newest") String sortBy,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sort sort = "oldest".equalsIgnoreCase(sortBy)
                ? Sort.by("timeCreate").ascending()
                : Sort.by("timeCreate").descending();
        Pageable pageable = PageRequest.of(page, 8, sort);

        String currentUserId = null;
        if (userDetails != null) {
            Users staff = usersRepo.findByEmail(userDetails.getUsername());
            if (staff != null) {
                currentUserId = staff.getId();
                model.addAttribute("staff", staff);
            }
        }

        Page<ForumPostDTO> postPage = requestService.searchPublicPosts(pageable, currentUserId, q, categoryId, departmentId);

        model.addAttribute("requests", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("allCategories", categoryRepo.findAll());
        model.addAttribute("allDepartments", departmentRepo.findAll());
        model.addAttribute("activeForumTab", "discussion");
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("sortBy", sortBy);

        return "staff/staff-forum";
    }

    @GetMapping("/forum/notification-management")
    public String viewForumNotificationManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "newest") String sortBy,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sort sort = "oldest".equalsIgnoreCase(sortBy)
                ? Sort.by("id").ascending()
                : Sort.by("id").descending();
        Pageable pageable = PageRequest.of(page, 8, sort);

        if (userDetails != null) {
            Users staff = usersRepo.findByEmail(userDetails.getUsername());
            if (staff != null) {
                model.addAttribute("staff", staff);
            }
        }

        Page<StaffAnnouncementCardDTO> postPage = annoucementService.searchAnnouncementCards(pageable, q, departmentId);

        model.addAttribute("announcements", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("allDepartments", departmentRepo.findAll());
        model.addAttribute("activeForumTab", "announcement");
        model.addAttribute("q", q);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("sortBy", sortBy);

        return "staff/staff-forum-notification-management";
    }

    @PostMapping("/forum/announcements/{id}/update")
    public String updateAnnouncement(@PathVariable("id") String announcementId,
                                     @RequestParam(required = false) String title,
                                     @RequestParam(required = false) String content,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) String departmentId,
                                     @RequestParam(defaultValue = "newest") String sortBy,
                                     @RequestParam(defaultValue = "0") int page,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Users staff = resolveStaff(userDetails);
        if (staff != null) {
            annoucementService.updateAnnouncement(announcementId, title, content, staff);
        }
        return buildAnnouncementManagementRedirect(q, departmentId, sortBy, page);
    }

    @PostMapping("/forum/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable("id") String announcementId,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) String departmentId,
                                     @RequestParam(defaultValue = "newest") String sortBy,
                                     @RequestParam(defaultValue = "0") int page,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Users staff = resolveStaff(userDetails);
        if (staff != null) {
            annoucementService.deleteAnnouncement(announcementId, staff);
        }
        return buildAnnouncementManagementRedirect(q, departmentId, sortBy, page);
    }

    private Users resolveStaff(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return usersRepo.findByEmail(userDetails.getUsername());
    }

    private String buildAnnouncementManagementRedirect(String q, String departmentId, String sortBy, int page) {
        String safeSort = (sortBy == null || sortBy.isBlank()) ? "newest" : sortBy;
        return "redirect:" + UriComponentsBuilder.fromPath("/staff/forum/notification-management")
                .queryParam("page", Math.max(page, 0))
                .queryParam("sortBy", safeSort)
                .queryParamIfPresent("q", optionalTrimmed(q))
                .queryParamIfPresent("departmentId", optionalTrimmed(departmentId))
                .build()
                .toUriString();
    }

    private java.util.Optional<String> optionalTrimmed(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(value.trim());
    }
}