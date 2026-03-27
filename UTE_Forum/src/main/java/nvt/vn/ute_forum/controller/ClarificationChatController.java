package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.MessageService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ClarificationChatController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClarificationChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ClarificationConversationService clarificationConversationService;
    private final MessageService messageService;
    private final UsersService usersService;

    public ClarificationChatController(SimpMessagingTemplate messagingTemplate,
                                       ClarificationConversationService clarificationConversationService,
                                       MessageService messageService,
                                       UsersService usersService) {
        this.messagingTemplate = messagingTemplate;
        this.clarificationConversationService = clarificationConversationService;
        this.messageService = messageService;
        this.usersService = usersService;
    }

    @MessageMapping("/clarification/send")
    public void sendMessage(ChatSendRequest payload, Principal principal) {
        try {
            if (payload == null || payload.requestId() == null || payload.requestId().isBlank()) {
                return;
            }

            Users user = resolvePrincipalName(principal == null ? null : principal.getName());
            if (user == null) {
                return;
            }

            List<MessageService.ChatAttachment> attachments = payload.attachments() == null
                    ? Collections.emptyList()
                    : payload.attachments();

            String content = payload.content() == null ? "" : payload.content().trim();
            if (content.isBlank() && attachments.isEmpty()) {
                return;
            }

            Optional<ClarificationConversation> conversationOptional = clarificationConversationService
                    .findOpenByRequestForStudent(payload.requestId(), user.getId());

            if (conversationOptional.isEmpty()) {
                return;
            }

            ClarificationConversation conversation = conversationOptional.get();
            MessageService.ChatMessageView savedMessage = messageService.saveMessage(
                    conversation,
                    payload.requestId(),
                    user,
                    content,
                    attachments,
                    user.getId()
            );

            messagingTemplate.convertAndSend("/topic/clarification/" + conversation.getId(), savedMessage);
        } catch (Exception ex) {
            LOGGER.error("Cannot send clarification message", ex);
        }
    }

    @PostMapping("/api/history/chat/upload")
    @ResponseBody
    public ResponseEntity<?> uploadChatFiles(@RequestParam("requestId") String requestId,
                                             @RequestParam("files") MultipartFile[] files,
                                             Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<ClarificationConversation> conversationOptional = clarificationConversationService
                .findOpenByRequestForStudent(requestId, user.getId());

        if (conversationOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cuộc hội thoại chưa được mở hoặc bạn không có quyền gửi"));
        }

        try {
            List<MessageService.ChatAttachment> uploaded = messageService.storeChatFiles(requestId, files);
            return ResponseEntity.ok(Map.of("attachments", uploaded));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể tải tệp lên: " + ex.getMessage()));
        }
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

        return resolvePrincipalName(email);
    }

    private Users resolvePrincipalName(String principalName) {
        if (principalName == null || principalName.isBlank() || "anonymousUser".equalsIgnoreCase(principalName)) {
            return null;
        }
        return usersService.getByEmail(principalName.trim());
    }

    public record ChatSendRequest(String requestId,
                                  String content,
                                  List<MessageService.ChatAttachment> attachments) {
    }
}



