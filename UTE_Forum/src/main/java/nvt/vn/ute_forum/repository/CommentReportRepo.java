package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReportRepo extends JpaRepository<CommentReport, String> {
}
