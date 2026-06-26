package nvt.vn.ute_forum.model.strategy;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeedbackSearchContext {

    private final Map<String, FeedbackSearchStrategy> strategies;

    public FeedbackSearchContext(Map<String, FeedbackSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public Page<Request> execute(String type, String value, Pageable pageable, Users user) {
        FeedbackSearchStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new RuntimeException("Không tìm thấy strategy: " + type);
        }

        return strategy.search(value, pageable, user);
    }
}