package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, String> {
    List<Department> findAll();
}
