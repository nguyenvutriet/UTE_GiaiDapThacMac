package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, String> {

    Users findByEmail(String email);

    Optional<Users> findOptionalByEmail(String email);

    List<Users> findByRole(String role);

    List<Users> findByRoleAndDepartment_Id(String role, String departmentId);

    List<Users> findByRoleAndDepartment_IdIn(String role, Collection<String> departmentIds);

    List<Users> findByDepartment(Department department);
}