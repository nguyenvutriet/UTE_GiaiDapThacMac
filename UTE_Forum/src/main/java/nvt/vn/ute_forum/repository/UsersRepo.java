package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, String> {

    public Users findByEmail(String email);
    Optional<Users> findOptionalByEmail(String email);
}
