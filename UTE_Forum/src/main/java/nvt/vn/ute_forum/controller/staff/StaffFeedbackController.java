package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.model.Users;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/staff")
public class StaffFeedbackController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

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
}