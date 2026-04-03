package nvt.vn.ute_forum.controller.student;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.MessageService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class SubmittedFeedbackHistoryChatController {

    @Autowired
    private ClarificationConversationService clarificationConversationService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UsersService usersService;

    @GetMapping("/api/history/chat/messages")
    @ResponseBody
    public ResponseEntity<?> getConversationMessages(@RequestParam("conversationId") String conversationId,
                                                     Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<ClarificationConversation> conversationOptional = clarificationConversationService
                .findByConversationIdForStudent(conversationId, user.getId());

        if (conversationOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cuộc hội thoại không tồn tại hoặc bạn không có quyền truy cập"));
        }

        ClarificationConversation conversation = conversationOptional.get();
        String requestId = conversation.getRequest() == null ? "" : safeValue(conversation.getRequest().getId());
        String subject = safeValue(conversation.getSubject());
        if (subject.isBlank() && conversation.getRequest() != null) {
            subject = safeValue(conversation.getRequest().getSubject());
        }
        if (subject.isBlank()) {
            subject = "Trao đổi";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("conversationId", conversation.getId());
        response.put("requestId", requestId);
        response.put("subject", subject);
        response.put("open", Boolean.TRUE.equals(conversation.getOpen()));
        response.put("messages", messageService.getConversationMessages(conversation.getId(), user.getId()));
        return ResponseEntity.ok(response);
    }

    private Users resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof UserPrincipal userPrincipal) {
            email = userPrincipal.getUsername();
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalName) {
            email = principalName;
        }

        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return null;
        }

        return usersService.getByEmail(email.trim());
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
