package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReportRepo extends JpaRepository<CommentReport, String> {
//    @Query("SELECT r FROM CommentReport r JOIN FETCH r.comment c JOIN FETCH c.request")
//    List<CommentReport> findAllWithComment();
    @Query("""
        SELECT r FROM CommentReport r
        JOIN FETCH r.comment c
        JOIN FETCH c.request req
        JOIN FETCH c.user cu
        JOIN FETCH r.student s
        LEFT JOIN FETCH r.admin a
    """)
    List<CommentReport> findAllWithComment();

    @Query("SELECT r FROM CommentReport r WHERE r.comment.id = :cmtId AND r.student.id = :userId")
    Optional<CommentReport> findExisting(String cmtId, String userId);

    boolean existsByComment_IdAndStudent_Id(String commentId, String studentId);
}
