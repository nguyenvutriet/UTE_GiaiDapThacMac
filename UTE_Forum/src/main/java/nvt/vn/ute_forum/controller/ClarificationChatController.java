package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.ClarificationConversationRepo;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.MessageService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ClarificationChatController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private ClarificationConversationService clarificationService;

    @Autowired
    private SimpMessagingTemplate messTemplate;

    @Autowired
    private UsersService userService;

    @Autowired
    private MessageService messService;

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
                    .findOpenByRequestForParticipant(payload.requestId(), user);

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

            // Broadcast đến CẢ HAI topic: sinh viên (/topic/clarification/) và staff (/topic/conversation/)
            messagingTemplate.convertAndSend("/topic/clarification/" + conversation.getId(), savedMessage);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), savedMessage);
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
                .findOpenByRequestForParticipant(requestId, user);

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

    @PostMapping("/staff/create-conversation")
    @ResponseBody
    public Map<String, String> createClarificationConversation(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Request request = requestService.getRequestById(req.get("requestId"));

        if (request == null) {
            throw new RuntimeException("Request not found");
        }

        String mess = req.get("initialMessage");
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(mess);
        message.setCreateAt(LocalDateTime.now());
        message.setReceiver(request.getUser());
        message.setSender(userPrincipal.getUser());
        messService.save(message);

        List<Message> messages = new ArrayList<>();
        messages.add(message);

        ClarificationConversation conversation = new ClarificationConversation();
        conversation.setId(clarificationService.createClarificationConversationId());
        conversation.setRequest(request);
        conversation.setOpen(true);
        conversation.setSubject(req.get("subject"));
        conversation.setCreateAt(LocalDate.now());
        conversation.setMessages(messages);

        clarificationConversationService.save(conversation);
        message.setClarificationConversation(conversation);
        messService.save(message);

        return Map.of("id", conversation.getId()); // 🔥 QUAN TRỌNG
    }

    @MessageMapping("/chat.send/{conversationId}")
    public void sendMessage(@DestinationVariable String conversationId,
                            Map<String, Object> payload,
                            Principal principal) {

        System.out.println("Payload nhận được: " + payload);

        String content = (String) payload.get("content");

        ClarificationConversation c =
                clarificationService.getConversationById(conversationId);

        Users sender = userService.getByEmail(principal.getName());

        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setCreateAt(LocalDateTime.now());
        message.setClarificationConversation(c);
        message.setSender(sender);

        Request request = requestService.getRequestById(c.getRequest().getId());
        Users receiver = request.getUser();

        message.setReceiver(receiver);

        messService.save(message);

        // DTO gửi về client
        MessageDTO dto = new MessageDTO(
                message.getId(),
                message.getContent(),
                message.getCreateAt().toString(),
                new SenderDTO(sender.getId(), sender.getFullName())
        );

        messTemplate.convertAndSend("/topic/conversation/" + conversationId, dto);
    }

    public record SenderDTO(String id, String fullName) {}

    public record MessageDTO(String id, String content, String createAt, SenderDTO sender) {}

    @PostMapping("/staff/close-conversation/{id}")
    @ResponseBody
    public ResponseEntity<?> closeConversation(@PathVariable String id) {
        ClarificationConversation c = clarificationService.getConversationById(id);
        if (c != null) {
            c.setOpen(false);
            clarificationService.save(c);
            return ResponseEntity.ok(Map.of("message", "Closed successfully"));
        }
        return ResponseEntity.notFound().build();
    }

}



