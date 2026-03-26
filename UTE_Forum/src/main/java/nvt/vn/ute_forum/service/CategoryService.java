package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryrepo) {
        this.categoryRepo = categoryrepo;
    }

    // Lấy tất cả danh mục
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    // Tìm kiếm danh mục
    public List<Category> searchBySubject(String keyword) {
        return categoryRepo.findBySubjectContainingIgnoreCase(keyword);
    }

    // Tạo danh mục mới
    public Category createCategory(String subject) {

        subject = subject.trim();

        if (categoryRepo.existsBySubjectIgnoreCase(subject)) {
            throw new RuntimeException("Danh mục đã tồn tại!");
        }

        Category cate = new Category(
                UUID.randomUUID().toString(),
                subject,
                true
        );
        return categoryRepo.save(cate);
    }

    // Cập nhật tên
    public Category updateSubject(String id, String newSubject) {

        newSubject = newSubject.trim();

        Optional<Category> optional = categoryRepo.findById(id);
        if (optional.isEmpty()) return null;

        Category category = optional.get();

        if (!category.getSubject().equalsIgnoreCase(newSubject)
                && categoryRepo.existsBySubjectIgnoreCase(newSubject)) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }

        category.setSubject(newSubject);
        return categoryRepo.save(category);
    }

    // Kích hoạt
    public Category activateCategory(String id) {
        Category category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            category.setActive(true);
            categoryRepo.save(category);
        }
        return category;
    }

    // Vô hiệu hóa
    public Category deactivateCategory(String id) {
        Category category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            category.setActive(false);
            categoryRepo.save(category);
        }
        return category;
    }
}
