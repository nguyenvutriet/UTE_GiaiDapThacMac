package nvt.vn.ute_forum.controller.annoucement;

import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.AnnoucementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class NavigationController {

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
    @GetMapping("/announcement") // Đường dẫn bà dùng để mở trang web
    public String showAnnouncementPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Fix lỗi "fullName null" (Lỗi đỏ lòm bà gửi nãy đó)
        // Phải lấy user từ DB và nạp vào model thì Sidebar mới hiện tên được
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            model.addAttribute("user", currentUser);
        }

        // 2. Fix lỗi "Ô Select trống trơn" (Mấy cái hình bà vừa gửi)
        // Phải nạp categories và departments vào model thì ô Select mới có dữ liệu để lặp (th:each)
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("departments", departmentRepo.findAll());

        return "student/announcementView"; // Trả về đúng file HTML của bà
    }


}