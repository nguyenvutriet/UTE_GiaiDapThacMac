package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Message;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.MessageRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {
    private static final String PAYLOAD_PREFIX = "CHAT_V1::";
    private static final String LEGACY_PAYLOAD_PREFIX = "CHAT_JSON::";
    private static final String ATTACHMENT_SECTION_SEPARATOR = "\n--FILES--\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MessageRepo messageRepo;
    private final IdGeneratorService idGeneratorService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public MessageService(MessageRepo messageRepo,
                          IdGeneratorService idGeneratorService) {
        this.messageRepo = messageRepo;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageView> getConversationMessages(String conversationId, String currentUserId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }

        return messageRepo.findByClarificationConversation_IdOrderByCreateAtAscIdAsc(conversationId)
                .stream()
                .map(message -> toView(message, currentUserId))
                .toList();
    }

    @Transactional
    public ChatMessageView saveMessage(ClarificationConversation conversation,
                                       Users sender,
                                       String text,
                                       List<ChatAttachment> attachments,
                                       String currentUserId) {
        Message message = new Message();
        message.setId(idGeneratorService.nextMessageId());
        message.setClarificationConversation(conversation);
        message.setUser(sender);
        message.setCreateAt(LocalDateTime.now());
        message.setContent(serializePayload(text, attachments));

        Message saved = messageRepo.save(message);
        return toView(saved, currentUserId);
    }

    public List<ChatAttachment> storeChatFiles(String requestId, MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);

        List<ChatAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalName = file.getOriginalFilename() == null
                    ? "file"
                    : Paths.get(file.getOriginalFilename()).getFileName().toString();
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }

            String normalizedRequestId = (requestId == null || requestId.isBlank()) ? "REQ_UNKNOWN" : requestId;
            String storedFileName = normalizedRequestId + "_CHAT_" + System.nanoTime() + extension;
            Path target = uploadRoot.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String contentType = file.getContentType();
            attachments.add(new ChatAttachment(
                    originalName,
                    "/uploads/" + storedFileName,
                    contentType == null ? "application/octet-stream" : contentType
            ));
        }
        return attachments;
    }

    private ChatMessageView toView(Message message, String currentUserId) {
        MessagePayload payload = deserializePayload(message.getContent());
        String senderId = message.getUser() == null ? "" : nullSafe(message.getUser().getId());
        String senderName = message.getUser() == null ? "Người dùng" : nullSafe(message.getUser().getFullName());
        String timeLabel = message.getCreateAt() == null ? "" : message.getCreateAt().format(DATE_FORMATTER);

        return new ChatMessageView(
                message.getId(),
                senderId,
                senderName,
                payload.text(),
                payload.attachments(),
                timeLabel,
                currentUserId != null && currentUserId.equals(senderId)
        );
    }

    private String serializePayload(String text, List<ChatAttachment> attachments) {
        String normalizedText = escape(normalizeText(text));
        List<ChatAttachment> sanitizedAttachments = safeAttachments(attachments);
        if (sanitizedAttachments.isEmpty()) {
            return PAYLOAD_PREFIX + normalizedText;
        }

        StringBuilder builder = new StringBuilder(PAYLOAD_PREFIX)
                .append(normalizedText)
                .append(ATTACHMENT_SECTION_SEPARATOR);

        for (ChatAttachment attachment : sanitizedAttachments) {
            builder.append(escape(nullSafe(attachment.name())))
                    .append('|')
                    .append(escape(attachment.url()))
                    .append('|')
                    .append(escape(nullSafe(attachment.type())))
                    .append('\n');
        }
        return builder.toString();
    }

    private MessagePayload deserializePayload(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return new MessagePayload("", Collections.emptyList());
        }

        if (rawContent.startsWith(LEGACY_PAYLOAD_PREFIX)) {
            String legacyText = rawContent.substring(LEGACY_PAYLOAD_PREFIX.length());
            return new MessagePayload(legacyText, Collections.emptyList());
        }

        if (!rawContent.startsWith(PAYLOAD_PREFIX)) {
            return new MessagePayload(rawContent, Collections.emptyList());
        }

        String payload = rawContent.substring(PAYLOAD_PREFIX.length());
        int separatorIndex = payload.indexOf(ATTACHMENT_SECTION_SEPARATOR);
        if (separatorIndex < 0) {
            return new MessagePayload(unescape(payload), Collections.emptyList());
        }

        String textPart = payload.substring(0, separatorIndex);
        String attachmentPart = payload.substring(separatorIndex + ATTACHMENT_SECTION_SEPARATOR.length());

        List<ChatAttachment> attachments = new ArrayList<>();
        String[] lines = attachmentPart.split("\\n");
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> fields = splitEscaped(line, '|', 3);
            if (fields.size() < 2) {
                continue;
            }

            String name = fields.get(0);
            String url = fields.get(1);
            String type = fields.size() >= 3 ? fields.get(2) : "application/octet-stream";
            if (url == null || url.isBlank()) {
                continue;
            }
            attachments.add(new ChatAttachment(
                    unescape(name),
                    unescape(url),
                    unescape(type)
            ));
        }

        return new MessagePayload(unescape(textPart), safeAttachments(attachments));
    }

    private List<ChatAttachment> safeAttachments(List<ChatAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }
        return attachments.stream()
                .filter(att -> att != null && att.url() != null && !att.url().isBlank())
                .map(att -> new ChatAttachment(
                        nullSafe(att.name()),
                        att.url(),
                        att.type() == null || att.type().isBlank() ? "application/octet-stream" : att.type()
                ))
                .toList();
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String escape(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String unescape(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) {
                if (ch == 'n') {
                    builder.append('\n');
                } else {
                    builder.append(ch);
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            builder.append(ch);
        }

        if (escaped) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private List<String> splitEscaped(String value, char separator, int maxParts) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (escaped) {
                current.append(ch);
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                current.append(ch);
                continue;
            }

            if (ch == separator && parts.size() < maxParts - 1) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        parts.add(current.toString());
        return parts;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public record ChatAttachment(String name, String url, String type) {
    }

    public record ChatMessageView(String id,
                                  String senderId,
                                  String senderName,
                                  String text,
                                  List<ChatAttachment> attachments,
                                  String time,
                                  boolean mine) {
    }

    private record MessagePayload(String text, List<ChatAttachment> attachments) {
    }

}
