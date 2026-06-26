package nvt.vn.ute_forum.model.facade;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import nvt.vn.ute_forum.service.CategoryService;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.RequestStatusHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public class FeedbackManagementFacade {

    @Autowired
    private RequestService requestService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClarificationConversationService clarificationConversationService;

    @Autowired
    private RequestStatusHistoryService requestStatusHistoryService;

    public void prepareFeedbackListPage(Model model,
                                        Pageable pageable,
                                        Users user,
                                        int page,
                                        String sortField,
                                        String sortDir) {

        Page<Request> requestPage = requestService.getAllFeedbacks(pageable, user);

        model.addAttribute("feedbacks", requestPage.getContent());
        model.addAttribute("categories", categoryService.getCategoriesByDepartment(user));
        model.addAttribute("statuses", requestService.getAvailableStatuses(user));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentUser", user);
    }

    public void prepareSearchPage(Model model,
                                  String keyword,
                                  Pageable pageable,
                                  Users user,
                                  int page,
                                  String categoryId,
                                  String status) {

        Page<Request> resultPage = requestService.searchFeedbacks(keyword, pageable, user);

        if (resultPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy");
        }

        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", user);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("categories", categoryService.getCategoriesByDepartment(user));
    }

    public void prepareFilterPage(Model model,
                                  String categoryId,
                                  String status,
                                  Pageable pageable,
                                  Users user,
                                  int page) {

        String finalCategoryId = "ALL".equals(categoryId) ? null : categoryId;
        String finalStatus = "ALL".equals(status) ? null : status;

        Page<Request> resultPage =
                requestService.getFeedbacks(finalCategoryId, finalStatus, pageable, user);

        model.addAttribute("feedbacks", resultPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resultPage.getTotalPages());
        model.addAttribute("selectedCategory", finalCategoryId);
        model.addAttribute("selectedStatus", finalStatus);
        model.addAttribute("categories", categoryService.getCategoriesByDepartment(user));
        model.addAttribute("statuses", requestService.getAvailableStatuses(user));
        model.addAttribute("currentUser", user);
    }

    public void prepareFeedbackDetailPage(Model model,
                                          String id,
                                          Users user) {

        Request request = requestService.getFeedbackDetail(id, user);

        ClarificationConversation conversation =
                clarificationConversationService.getClarificationConversation(request.getId());

        model.addAttribute("conversation", conversation);
        model.addAttribute("feedback", request);
        model.addAttribute("currentUser", user);
        model.addAttribute("forwardLogs", request.getForwardingLogs());
        model.addAttribute("histories",
                requestStatusHistoryService.getByRequestId(request.getId()));
        model.addAttribute("departments", departmentRepo.findAll());
    }
}