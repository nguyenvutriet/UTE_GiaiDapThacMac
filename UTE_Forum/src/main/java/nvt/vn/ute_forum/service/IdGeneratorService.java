package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.FileAttachmentRepo;
import nvt.vn.ute_forum.repository.MessageRepo;
import nvt.vn.ute_forum.repository.NotificationRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdGeneratorService {

    private final RequestRepo requestRepo;
    private final FileAttachmentRepo fileAttachmentRepo;
    private final NotificationRepo notificationRepo;
    private final MessageRepo messageRepo;

    private long requestSequence = -1;
    private long fileAttachmentSequence = -1;
    private long notificationSequence = -1;
    private long messageSequence = -1;

    public IdGeneratorService(RequestRepo requestRepo,
                              FileAttachmentRepo fileAttachmentRepo,
                              NotificationRepo notificationRepo,
                              MessageRepo messageRepo) {
        this.requestRepo = requestRepo;
        this.fileAttachmentRepo = fileAttachmentRepo;
        this.notificationRepo = notificationRepo;
        this.messageRepo = messageRepo;
    }

    @Transactional(readOnly = true)
    public synchronized String nextRequestId() {
        if (requestSequence < 0) {
            requestSequence = safeSequence(requestRepo.findMaxRequestSequence());
        }
        requestSequence++;
        return "REQ_" + requestSequence;
    }

    @Transactional(readOnly = true)
    public synchronized String nextFileAttachmentId() {
        if (fileAttachmentSequence < 0) {
            fileAttachmentSequence = safeSequence(fileAttachmentRepo.findMaxFileAttachmentSequence());
        }
        fileAttachmentSequence++;
        return "FA_" + fileAttachmentSequence;
    }

    @Transactional(readOnly = true)
    public synchronized String nextNotificationId() {
        if (notificationSequence < 0) {
            notificationSequence = safeSequence(notificationRepo.findMaxNotificationSequence());
        }
        notificationSequence++;
        return "NOTI_" + notificationSequence;
    }

    @Transactional(readOnly = true)
    public synchronized String nextMessageId() {
        if (messageSequence < 0) {
            messageSequence = safeSequence(messageRepo.findMaxMessageSequence());
        }
        messageSequence++;
        return "MSG_" + messageSequence;
    }

    private long safeSequence(Long value) {
        return value == null ? 0L : value;
    }
}

