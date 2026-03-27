package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.repository.ClarificationConversationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClarificationConversationService {

    @Autowired
    private ClarificationConversationRepo clarificationConversationRepo;

    public ClarificationConversation getClarificationConversation(String requestId){
        return clarificationConversationRepo.findByRequestId(requestId);
    }

}
