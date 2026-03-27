package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.ClarificationConversationRepo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ClarificationConversationService {

    private final ClarificationConversationRepo clarificationConversationRepo;

    public ClarificationConversationService(ClarificationConversationRepo clarificationConversationRepo) {
        this.clarificationConversationRepo = clarificationConversationRepo;
    }

    public Optional<ClarificationConversation> findByRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return Optional.empty();
        }
        return clarificationConversationRepo.findByRequest_Id(requestId);
    }

    public Optional<ClarificationConversation> findOpenByRequestForStudent(String requestId, String studentId) {
        if (requestId == null || requestId.isBlank() || studentId == null || studentId.isBlank()) {
            return Optional.empty();
        }
        return clarificationConversationRepo.findOpenByRequestIdAndStudentId(requestId, studentId);
    }

    public boolean isConversationOpenForStudent(ClarificationConversation conversation, String studentId) {
        if (conversation == null || !Boolean.TRUE.equals(conversation.getOpen())) {
            return false;
        }
        if (conversation.getRequest() == null || conversation.getRequest().getUser() == null) {
            return false;
        }
        return studentId != null && studentId.equals(conversation.getRequest().getUser().getId());
    }

    public List<ClarificationConversation> getOpenConversationsByStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return Collections.emptyList();
        }
        return clarificationConversationRepo.findOpenConversationsByStudentId(studentId);
    }

    public Optional<ClarificationConversation> findOpenByConversationIdForStudent(String conversationId, String studentId) {
        if (conversationId == null || conversationId.isBlank() || studentId == null || studentId.isBlank()) {
            return Optional.empty();
        }
        return clarificationConversationRepo.findOpenByIdAndStudentId(conversationId, studentId);
    }

    public ClarificationConversation getClarificationConversation(String requestId){
        return clarificationConversationRepo.findByRequestId(requestId);
    }

    public Optional<ClarificationConversation> findOpenByRequestForParticipant(String requestId, Users participant) {
        if (requestId == null || requestId.isBlank() || participant == null || participant.getId() == null) {
            return Optional.empty();
        }

        String role = participant.getRole() == null ? "" : participant.getRole().trim();
        if ("ROLE_STUDENT".equalsIgnoreCase(role)) {
            return clarificationConversationRepo.findOpenByRequestIdAndStudentId(requestId, participant.getId());
        }

        if ("ROLE_DEPARTMENT".equalsIgnoreCase(role)) {
            return clarificationConversationRepo.findOpenByRequestIdAndDepartmentStaffId(requestId, participant.getId());
        }

        return Optional.empty();
    }

}
