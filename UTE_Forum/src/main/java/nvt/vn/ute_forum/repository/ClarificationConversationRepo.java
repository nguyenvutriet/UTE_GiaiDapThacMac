package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.ClarificationConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClarificationConversationRepo extends JpaRepository<ClarificationConversation, String> {

    ClarificationConversation findByRequestId(String requestId);
}
