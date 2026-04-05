package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.AnnoucementService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
public class StaffAnnouncementController {

    private final UsersRepo usersRepo;
    private final DepartmentRepo departmentRepo;
    private final AnnouncementRepo announcementRepo;
    private final AnnoucementService annoucementService;

    public StaffAnnouncementController(UsersRepo usersRepo,
                                       DepartmentRepo departmentRepo,
                                       AnnouncementRepo announcementRepo,
                                       AnnoucementService annoucementService) {
        this.usersRepo = usersRepo;
        this.departmentRepo = departmentRepo;
        this.announcementRepo = announcementRepo;
        this.annoucementService = annoucementService;
    }

    @GetMapping("/announcement-management")
    public String showAnnouncementManagementPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Users currentUser = resolveStaffUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/api/forum/view";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("departments", departmentRepo.findAll());
        return "staff/staff-announcement-management";
    }

    @GetMapping("/announcement-management/create")
    public String showCreateAnnouncementPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Users currentUser = resolveStaffUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/api/forum/view";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("departments", departmentRepo.findAll());
        model.addAttribute("currentDepartmentId", currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null);
        return "staff/staff-announcement-create";
    }

    @GetMapping("/announcement-management/{announcementId}")
    public String showAnnouncementDetail(@PathVariable String announcementId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        Users currentUser = resolveStaffUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/api/forum/view";
        }

        Announcement announcement = announcementRepo.findById(announcementId).orElse(null);
        if (announcement == null) {
            return "redirect:/staff/announcement-management";
        }

        String currentDepartmentId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
        String announcementDepartmentId = (announcement.getUser() != null && announcement.getUser().getDepartment() != null)
                ? announcement.getUser().getDepartment().getId()
                : null;

        if (currentDepartmentId == null || !currentDepartmentId.equals(announcementDepartmentId)) {
            return "redirect:/staff/announcement-management";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("ann", annoucementService.mapToDTO(announcement));
        return "staff/staff-announcement-detail";
    }

    private Users resolveStaffUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return usersRepo.findByEmail(userDetails.getUsername());
    }
}

