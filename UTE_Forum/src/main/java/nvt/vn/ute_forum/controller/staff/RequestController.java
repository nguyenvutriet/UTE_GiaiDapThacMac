package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.Request;
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
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

    @GetMapping("/list-feedbacks")
    public String getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 🔥 1. Sort
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 12, sort);

        // 🔥 2. Lấy user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // 🔥 3. Gọi service (role xử lý bên service)
        Page<Request> requestPage = requestService.getAllFeedbacks(pageable, user);

        // 🔥 4. Đẩy data ra view
        model.addAttribute("feedbacks", requestPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentUser", user);

        return "staff/staff-list";
    }

    @GetMapping("/feedback-detail")
    public String getFeedbackDetail(
            @RequestParam("id") String id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 🔥 lấy user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // 🔥 truyền user vào service
        Request request = requestService.getFeedbackDetail(id, user);

        model.addAttribute("feedback", request);
        model.addAttribute("currentUser", user);

        return "staff/feedback-detail";
    }

    @GetMapping("/search-feedbacks")
    public String searchFeedbacks(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "timeCreate") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // sort
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 12, sort);

        // user hiện tại
        Users user = usersRepo.findByEmail(userDetails.getUsername());

        // gọi service (đúng sequence)
        Page<Request> resultPage = requestService.searchFeedbacks(keyword, pageable, user);

        // 🔥 alt flow (match sequence diagram)
        if (resultPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy");
        }

        // data
        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", user);

        return "staff/staff-list";
    }
}