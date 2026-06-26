package nvt.vn.ute_forum.model.strategy;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component("STATUS")
public class StatusFilterStrategy implements FeedbackSearchStrategy {

    @Autowired
    private RequestRepo requestRepo;

    @Override
    public Page<Request> search(String value, Pageable pageable, Users user) {
        if ("ALL".equals(value)) {
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return requestRepo.findAll(pageable);
            }

            if ("ROLE_DEPARTMENT".equals(user.getRole())) {
                return requestRepo.findByDepartment_Id(
                        user.getDepartment().getId(),
                        pageable
                );
            }
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return requestRepo.findByCurrentStatus(value, pageable);
        }

        if ("ROLE_DEPARTMENT".equals(user.getRole())) {
            return requestRepo.findByCurrentStatusAndDepartment_Id(
                    value,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }
}