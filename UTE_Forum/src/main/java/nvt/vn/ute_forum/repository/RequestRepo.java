package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepo extends JpaRepository<Request, String> {
}
