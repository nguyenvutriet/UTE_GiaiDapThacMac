package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.RequestStatusHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestStatusHistoryService {

    @Autowired
    private RequestStatusHistoryRepo requestStatushistoryRepo;

}
