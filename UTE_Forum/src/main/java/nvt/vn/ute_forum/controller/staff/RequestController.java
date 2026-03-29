package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.repository.CategoryRepo;
import nvt.vn.ute_forum.service.CategoryService;
import nvt.vn.ute_forum.service.ClarificationConversationService;
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

import java.util.List;

@Controller
@RequestMapping("/staff")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ClarificationConversationService clarificationConversationService;

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
        List<Category> categories =
                categoryService.getCategoriesByDepartment(user);

        model.addAttribute("categories", categories);
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

        ClarificationConversation conversation = clarificationConversationService.getClarificationConversation(request.getId());

        model.addAttribute("conversation", conversation);
        model.addAttribute("feedback", request);
        model.addAttribute("currentUser", user);
        model.addAttribute("forwardLogs", request.getForwardingLogs());

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

    @GetMapping("/filter-feedbacks")
    public String filterFeedbacks(
            @RequestParam("categoryId") String categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        Users user = usersRepo.findByEmail(userDetails.getUsername());

        Pageable pageable = PageRequest.of(page, 12);

        Page<Request> resultPage =
                requestService.filterFeedbacks(categoryId, pageable, user);

        // alt flow giống search
        if (resultPage.isEmpty()) {
            model.addAttribute("message", "Không có dữ liệu");
        }

        List<Category> categories =
                categoryService.getCategoriesByDepartment(user);

        model.addAttribute("categories", categories);
        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("selectedCategory", categoryId);
        Category category = categoryRepo.findById(categoryId).orElse(null);
        if (category != null) {
            model.addAttribute("selectedCategoryName", category.getSubject());
        }
        model.addAttribute("currentUser", user);

        return "staff/staff-list";
    }
}