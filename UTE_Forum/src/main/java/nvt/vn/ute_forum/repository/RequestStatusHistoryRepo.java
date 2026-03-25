package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestStatusHistoryRepo extends JpaRepository<RequestStatusHistory, String> {
    List<RequestStatusHistory> findByRequest_IdOrderByCreateAtAsc(String requestId);
}
