package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    @Autowired
    private RequestRepo requestRepo;

    public List<Request> getRequestsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return requestRepo.findByUser_IdOrderByTimeCreateDesc(userId);
    }

    public Optional<Request> getRequestByIdAndUserId(String requestId, String userId) {
        if (requestId == null || requestId.isBlank() || userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return requestRepo.findByIdAndUser_Id(requestId, userId);
    }

    public Request saveOrUpdate(Request request) {
        return requestRepo.save(request);
    }

}
