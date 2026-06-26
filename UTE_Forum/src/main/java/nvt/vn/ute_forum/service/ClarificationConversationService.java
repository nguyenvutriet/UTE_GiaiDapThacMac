package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.ClarificationConversationRepo;
import nvt.vn.ute_forum.repository.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nvt.vn.ute_forum.model.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClarificationConversationService {

    @Autowired
    private MessageService messageService;

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

    public List<ClarificationConversation> getConversationsByStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return Collections.emptyList();
        }
        return clarificationConversationRepo.findConversationsByStudentId(studentId);
    }

    public Optional<ClarificationConversation> findOpenByConversationIdForStudent(String conversationId, String studentId) {
        if (conversationId == null || conversationId.isBlank() || studentId == null || studentId.isBlank()) {
            return Optional.empty();
        }
        return clarificationConversationRepo.findOpenByIdAndStudentId(conversationId, studentId);
    }

    public Optional<ClarificationConversation> findByConversationIdForStudent(String conversationId, String studentId) {
        if (conversationId == null || conversationId.isBlank() || studentId == null || studentId.isBlank()) {
            return Optional.empty();
        }
        return clarificationConversationRepo.findByIdAndStudentId(conversationId, studentId);
    }

    public List<ClarificationConversation> findByRequestForStudent(String requestId, String studentId) {
        if (requestId == null || requestId.isBlank() || studentId == null || studentId.isBlank()) {
            return Collections.emptyList();
        }
        return clarificationConversationRepo.findByRequestIdAndStudentId(requestId, studentId);
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

    public String createClarificationConversationId(){
        String id = "CC" + System.currentTimeMillis();
        while (clarificationConversationRepo.existsById(id)) {
            id = "CC" + System.currentTimeMillis();
        }
        return id;
    }

    public void save(ClarificationConversation conversation) {
        if (conversation != null) {
            clarificationConversationRepo.save(conversation);
        }
    }


//    public ClarificationConversation getConversationById(String conversationId) {
//        if (conversationId == null || conversationId.isBlank()) {
//            return null;
//        }
//        return clarificationConversationRepo.findById(conversationId).orElse(null);
//    }

    @Transactional
    public ClarificationConversation getConversationById(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return null;
        }

        ClarificationConversation conversation =
                clarificationConversationRepo.findById(conversationId).orElse(null);

        if (conversation != null && conversation.getMessages() != null) {
            conversation.getMessages().sort(Comparator.comparing(Message::getCreateAt));
        }

        return conversation;
    }

    public ClarificationConversation createConversation(String content, Users sender, Request request, String subject) {

        Message mess = new Message();
        mess.setId(UUID.randomUUID().toString());
        mess.setContent(content);
        mess.setSender(sender);
        mess.setReceiver(request.getUser());
        mess.setCreateAt(LocalDateTime.now());
        messageService.save(mess);

        List<Message> messages = new ArrayList<>();
        messages.add(mess);

        ClarificationConversation conversation = new ClarificationConversation();
        conversation.setId(createClarificationConversationId());
        conversation.setRequest(request);
        conversation.setSubject(subject);
        conversation.setOpen(true);
        conversation.setCreateAt(LocalDate.now());
        conversation.setMessages(messages);

        clarificationConversationRepo.save(conversation);
        mess.setClarificationConversation(conversation);
        messageService.save(mess);

        return conversation;
    }

    public void closeConversation(String conversationId) {
        ClarificationConversation conversation = clarificationConversationRepo.findById(conversationId).orElse(null);
        if(conversation != null){
            conversation.setOpen(false);
            clarificationConversationRepo.save(conversation);
        }
    }
}
