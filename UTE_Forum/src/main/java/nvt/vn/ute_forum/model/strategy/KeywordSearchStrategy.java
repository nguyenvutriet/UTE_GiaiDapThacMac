package nvt.vn.ute_forum.model.strategy;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component("KEYWORD")
public class KeywordSearchStrategy implements FeedbackSearchStrategy {

    @Autowired
    private RequestRepo requestRepo;

    @Override
    public Page<Request> search(String value, Pageable pageable, Users user) {
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return requestRepo.findByContentContaining(value, pageable);
        }

        if ("ROLE_DEPARTMENT".equals(user.getRole())) {
            return requestRepo.findByContentContainingAndDepartment_Id(
                    value,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }
}