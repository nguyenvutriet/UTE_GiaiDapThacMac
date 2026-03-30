package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.model.Announcement;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/adannouncement") // Đổi lại path cho đẹp, không để /api

public class AdAnnounController {
    @Autowired
    private AnnouncementRepo announcementRepository;

    @Autowired
    private AnnoucementService announcementService;
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private DepartmentRepo departmentRepo;


    // ĐÂY LÀ VÍ DỤ - Bà tìm cái hàm tương ứng trong code của mình nhé
    @GetMapping("/") // Đường dẫn bà dùng để mở trang web
    public String showAnnouncementPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        // CHỖ NÀY QUAN TRỌNG: Lấy user để hiện tên Admin và ID Admin lên Sidebar
        if (userDetails != null) {
            // Tìm user trong DB bằng email (username trong Spring Security thường là email)
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());

            if (currentUser != null) {
                model.addAttribute("user", currentUser); // Khớp với ${user.fullName} trong HTML
            } else {
                // Nếu không tìm thấy user, cho một object rỗng để không bị sập trang
                model.addAttribute("user", new Users());
            }
        }

        // 2. Fix lỗi "Ô Select trống trơn" (Mấy cái hình bà vừa gửi)
        // Phải nạp categories và departments vào model thì ô Select mới có dữ liệu để lặp (th:each)
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("departments", departmentRepo.findAll());

        return "admin/announcementView"; // Trả về đúng file HTML của bà
    }

    @GetMapping("/detail/{id}")
    public String showDetailPage(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        // CHỖ NÀY QUAN TRỌNG: Lấy user để hiện tên Admin và ID Admin lên Sidebar
        if (userDetails != null) {
            // Tìm user trong DB bằng email (username trong Spring Security thường là email)
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());

            if (currentUser != null) {
                model.addAttribute("user", currentUser); // Khớp với ${user.fullName} trong HTML
            } else {
                // Nếu không tìm thấy user, cho một object rỗng để không bị sập trang
                model.addAttribute("user", new Users());
            }
        }

        return announcementRepository.findById(id)
                .map(ann -> {
                    // Đổ dữ liệu vào Model với tên là "ann"
                    model.addAttribute("ann", announcementService.mapToDTO(ann));
                    return "admin/announcementDetail";
                })
                .orElse("redirect:/adannouncement");
    }


}
