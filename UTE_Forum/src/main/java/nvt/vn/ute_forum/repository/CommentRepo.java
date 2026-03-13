package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepo extends JpaRepository<Comment, String> {
}
