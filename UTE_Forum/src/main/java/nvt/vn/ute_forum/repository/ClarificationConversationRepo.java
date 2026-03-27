package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.ClarificationConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClarificationConversationRepo extends JpaRepository<ClarificationConversation, String> {
	Optional<ClarificationConversation> findByRequest_Id(String requestId);

	@Query("""
			SELECT cc
			FROM ClarificationConversation cc
			JOIN cc.request r
			JOIN r.user u
			WHERE cc.isOpen = true
			  AND r.id = :requestId
			  AND u.id = :studentId
			""")
	Optional<ClarificationConversation> findOpenByRequestIdAndStudentId(@Param("requestId") String requestId,
																		 @Param("studentId") String studentId);

	@Query("""
			SELECT cc
			FROM ClarificationConversation cc
			JOIN FETCH cc.request r
			JOIN r.user u
			WHERE cc.isOpen = true
			  AND u.id = :studentId
			ORDER BY cc.createAt DESC
			""")
	List<ClarificationConversation> findOpenConversationsByStudentId(@Param("studentId") String studentId);

	@Query("""
			SELECT cc
			FROM ClarificationConversation cc
			JOIN FETCH cc.request r
			JOIN r.user u
			WHERE cc.isOpen = true
			  AND cc.id = :conversationId
			  AND u.id = :studentId
			""")
	Optional<ClarificationConversation> findOpenByIdAndStudentId(@Param("conversationId") String conversationId,
																 @Param("studentId") String studentId);
}
