package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, String> {

    // Lấy danh sách comment theo ID của Request (Bài viết)
    // Sắp xếp theo thứ tự mới nhất hoặc cũ nhất tùy bạn (thường comment cũ hiện trước)
    List<Comment> findByRequestId(String requestId);

    // Nếu muốn đếm số lượng comment để hiển thị ngoài giao diện
    long countByRequest_Id(String requestId);
}