package nvt.vn.ute_forum.controller.forum;

//import nvt.vn.ute_forum.service.RequestService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller; // Dùng @Controller
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import nvt.vn.ute_forum.model.Users; // Class User entity của bạn
//
//@Controller
//@RequestMapping("/api/forum")
//public class ForumController {
//
//    @Autowired
//    private RequestService requestService;
//
//    @GetMapping("/view")
//    public String showForumPage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
//        // 1. Lấy dữ liệu từ Service của bạn
//        var posts = requestService.getPublicPosts();
//
//        // 2. Đẩy vào Model với tên "requests" (giống trong file HTML của bạn)
//        model.addAttribute("requests", posts);
//        // 2. Truyền thông tin user đang đăng nhập
//        // 2. Kiểm tra principal và lấy Users ra
//        if (principal != null) {
//            // Giả sử trong class UserPrincipal bạn có hàm getUser() hoặc field user
//            model.addAttribute("user", principal.getUser());
//        }
//        System.out.println("Current User: " + currentUser); // Nếu in ra null thì lỗi do Security config
//
//        // 3. Trả về đúng đường dẫn file HTML: templates/student/forumView.html
//        return "student/forumView";
//    }
//}


import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo; // Import Repo của bạn
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
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo; // Inject Repo để tìm User từ DB

    @GetMapping("/view")
    public String showForumPage(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Phân trang: 5 bài mỗi trang, sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, 10, Sort.by("timeCreate").descending());
        Page<ForumPostDTO> postPage = requestService.getPublicPosts(pageable);

        // 2. Đưa vào Model
        model.addAttribute("requests", postPage.getContent()); // Danh sách bài
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        // 3. User đăng nhập (như mẫu Checkout của bạn)
        if (userDetails != null) {
            Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
            model.addAttribute("user", currentUser);
        }

        return "student/forumView";
    }
}