package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.CategoryService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService service;
    private final UsersService usersService;

    public CategoryController(CategoryService service, UsersService usersService) {
        this.service = service;
        this.usersService = usersService;
    }

    @GetMapping
    public String showCategoryPage(Model model, Principal principal) {

        String email = principal.getName();
        Users user = usersService.getByEmail(email);

        model.addAttribute("user", user);

        model.addAttribute("categories", service.getAllCategories());

        return "admin/category-management";
    }

    @GetMapping("/search")
    public String searchCategory(@RequestParam String keyword, Model model) {
        List<Category> list = service.searchBySubject(keyword);
        model.addAttribute("categories", list);
        model.addAttribute("keyword", keyword);
        return "admin/category-management";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam String subject,
                                 RedirectAttributes ra) {
        try {
            service.createCategory(subject);
            ra.addFlashAttribute("success", "Thêm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/category";
    }

    @PostMapping("/update")
    public String updateCategory(@RequestParam String id,
                                 @RequestParam String subject,
                                 RedirectAttributes ra) {
        try {
            service.updateSubject(id, subject);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/category";
    }

    // Kích hoạt
    @PostMapping("/activate/{id}")
    public String activate(@PathVariable String id) {
        service.activateCategory(id);
        return "redirect:/admin/category";
    }

    // Vô hiệu hóa
    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable String id) {
        service.deactivateCategory(id);
        return "redirect:/admin/category";
    }
}