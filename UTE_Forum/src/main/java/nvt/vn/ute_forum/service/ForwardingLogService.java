package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.ForwardingLog;
import nvt.vn.ute_forum.repository.ForwardingLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ForwardingLogService {

    @Autowired
    private ForwardingLogRepo forwardingLogRepo;

    public List<ForwardingLog> getByRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return Collections.emptyList();
        }
        return forwardingLogRepo.findByRequest_IdOrderByForwardAtAsc(requestId);
    }

}
