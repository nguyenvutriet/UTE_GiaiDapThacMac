package nvt.vn.ute_forum.controller.annoucement;

import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class NavigationController {

    @Autowired
    private UsersRepo usersRepo; // Má nhớ inject cái Repo của má vào

    @GetMapping("/announcement")
    public String showAnnouncementPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // Lấy thông tin user từ database dựa trên email/username đã đăng nhập
            Users user = usersRepo.findByEmail(userDetails.getUsername());
            // Đưa object user vào model với key là "user" để HTML đọc được
            model.addAttribute("user", user);
        } else {
            // Nếu chưa đăng nhập mà lỡ vào đây thì cho về trang login hoặc xử lý tùy má
            return "redirect:/login";
        }

        return "student/announcementView";
    }


}