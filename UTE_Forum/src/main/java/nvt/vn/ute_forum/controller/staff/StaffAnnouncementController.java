package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
public class StaffAnnouncementController {

    private final UsersRepo usersRepo;
    private final DepartmentRepo departmentRepo;

    public StaffAnnouncementController(UsersRepo usersRepo, DepartmentRepo departmentRepo) {
        this.usersRepo = usersRepo;
        this.departmentRepo = departmentRepo;
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

    private Users resolveStaffUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return usersRepo.findByEmail(userDetails.getUsername());
    }
}

