package nvt.vn.ute_forum.model.strategy;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackSearchStrategy {
    Page<Request> search(String value, Pageable pageable, Users user);
}