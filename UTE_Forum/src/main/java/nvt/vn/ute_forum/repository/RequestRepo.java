package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepo extends JpaRepository<Request, String> {
    List<Request> findByUser_IdOrderByTimeCreateDesc(String userId);
    Optional<Request> findByIdAndUser_Id(String id, String userId);
}
