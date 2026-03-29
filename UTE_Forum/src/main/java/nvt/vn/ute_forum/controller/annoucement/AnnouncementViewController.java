package nvt.vn.ute_forum.controller.annoucement;

import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.AnnoucementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller; // QUAN TRỌNG
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller // Dùng cái này để trả về View (HTML)
@RequestMapping("/announcement") // Đổi lại path cho đẹp, không để /api
public class AnnouncementViewController {

    @Autowired
    private AnnouncementRepo announcementRepository;

    @Autowired
    private AnnoucementService announcementService;
    @Autowired
    private UsersRepo usersRepo;




    @GetMapping("/detail/{id}")
    public String showDetailPage(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
        model.addAttribute("user", currentUser);

        return announcementRepository.findById(id)
                .map(ann -> {
                    // Đổ dữ liệu vào Model với tên là "ann"
                    model.addAttribute("ann", announcementService.mapToDTO(ann));
                    return "student/announcementDetail";
                })
                .orElse("redirect:/announcement");
    }

    
}