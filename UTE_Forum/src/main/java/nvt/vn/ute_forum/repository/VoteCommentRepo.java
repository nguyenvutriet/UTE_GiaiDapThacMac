package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.VoteComment;
import nvt.vn.ute_forum.model.VoteCommentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteCommentRepo extends JpaRepository<VoteComment, VoteCommentId> {
    // Đếm tổng số lượt reaction của 1 comment
    long countByIdCommentId(String commentId);
    @Query("""
SELECT v.type, COUNT(v)
FROM VoteComment v
WHERE v.comment.id = :commentId
GROUP BY v.type
""")
    List<Object[]> countReactionsByCommentId(@Param("commentId") String commentId);

    // Tìm reaction của 1 user cụ thể trên 1 comment cụ thể
    Optional<VoteComment> findByIdUserIdAndIdCommentId(String userId, String commentId);
}
