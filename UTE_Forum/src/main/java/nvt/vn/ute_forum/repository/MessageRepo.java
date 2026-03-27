package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, String> {
	List<Message> findByClarificationConversation_IdOrderByCreateAtAscIdAsc(String conversationId);

	@Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 5) AS UNSIGNED)), 0) FROM message WHERE id REGEXP '^MSG_[0-9]+$'", nativeQuery = true)
	Long findMaxMessageSequence();
}
