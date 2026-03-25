package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.ForwardingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForwardingLogRepo extends JpaRepository<ForwardingLog, String> {
    List<ForwardingLog> findByRequest_IdOrderByForwardAtAsc(String requestId);
}
