package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.RequestStatusHistory;
import nvt.vn.ute_forum.repository.RequestStatusHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class RequestStatusHistoryService {

    @Autowired
    private RequestStatusHistoryRepo requestStatushistoryRepo;

    public List<RequestStatusHistory> getByRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return Collections.emptyList();
        }
        return requestStatushistoryRepo.findByRequest_IdOrderByCreateAtAsc(requestId);
    }

    public void createInitialStatus(Request request, String status) {
        if (request == null || request.getId() == null || request.getId().isBlank()) {
            return;
        }

        RequestStatusHistory history = new RequestStatusHistory();
        history.setId("RSH_" + System.nanoTime());
        history.setStatus(status);
        history.setCreateAt(LocalDateTime.now());
        history.setRequest(request);
        requestStatushistoryRepo.save(history);
    }

    public void createForwardStatus(Request request) {

        RequestStatusHistory history = new RequestStatusHistory();
        history.setId("RSH_" + System.nanoTime());
        history.setStatus("FORWARDING");
        history.setCreateAt(LocalDateTime.now());
        history.setRequest(request);

        requestStatushistoryRepo.save(history);
    }

}
