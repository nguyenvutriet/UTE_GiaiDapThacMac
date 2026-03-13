package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepo extends JpaRepository<Message, String> {
}
