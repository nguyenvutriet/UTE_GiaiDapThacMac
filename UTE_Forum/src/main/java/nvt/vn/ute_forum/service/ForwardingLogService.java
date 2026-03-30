package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.ForwardingLog;
import nvt.vn.ute_forum.repository.ForwardingLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import nvt.vn.ute_forum.model.*;
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

    public void createLog(Request request,
                          Department fromDept,
                          Department toDept,
                          String note,
                          Users user) {

        ForwardingLog log = new ForwardingLog();
        log.setId("FW_" + System.nanoTime());
        log.setForwardAt(LocalDateTime.now());
        log.setMessage("Chuyển tiếp phản hồi");
        log.setNote(note);

        log.setRequest(request);
        log.setFromdepartment(fromDept);
        log.setTodepartment(toDept);
        log.setUser(user);

        forwardingLogRepo.save(log);
    }

}
