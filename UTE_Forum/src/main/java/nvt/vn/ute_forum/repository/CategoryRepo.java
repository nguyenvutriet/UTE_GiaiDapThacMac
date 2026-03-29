package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category, String> {
    List<Category> findBySubjectContainingIgnoreCase(String keyword);

    List<Category> findByIsActive(Boolean isActive);

    boolean existsBySubjectIgnoreCase(String subject);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.requests")
    List<Category> findAllWithRequestCount();

    @Query("""
        SELECT DISTINCT c FROM Category c
        JOIN c.requests r
        WHERE r.department.id = :departmentId
        """)
    List<Category> findCategoriesByDepartment(@Param("departmentId") String departmentId);
}
