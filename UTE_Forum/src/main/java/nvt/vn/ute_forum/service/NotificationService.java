package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    public List<Notification> getByUserId(String userId) {
        return notificationRepo.findByUsers_IdOrderByCreateAtDesc(userId);
    }

    public long countUnreadByUserId(String userId) {
        return notificationRepo.countByUsers_IdAndIsReadFalse(userId);
    }

    public Optional<Notification> getByIdForUser(String notificationId, String userId) {
        return notificationRepo.findByIdAndUsers_Id(notificationId, userId);
    }

    public void markAsRead(Notification notification) {
        if (notification == null || Boolean.TRUE.equals(notification.getRead())) {
            return;
        }
        notification.setRead(true);
        notificationRepo.save(notification);
    }

    public void notifyForumComment(Users actor, Request request, String commentContent) {
        if (request == null || request.getUser() == null || actor == null) {
            return;
        }
        Users receiver = request.getUser();
        if (receiver.getId() != null && receiver.getId().equals(actor.getId())) {
            return;
        }

        String actorName = actor.getFullName() == null ? "Nguoi dung" : actor.getFullName();
        String requestSubject = request.getSubject() == null ? "bai viet" : request.getSubject();
        String safeComment = (commentContent == null || commentContent.isBlank()) ? "" : (": \"" + commentContent.trim() + "\"");

        upsertForumNotification(
                "COMMENT_FORUM_POST_NOTIFICATION",
                "Binh luan moi",
                actorName + " da binh luan tren bai viet \"" + requestSubject + "\"" + safeComment,
                request.getId(),
                receiver
        );
    }

    public void notifyForumVote(Users actor, Request request, ReactionType reactionType) {
        if (request == null || request.getUser() == null || actor == null || reactionType == null) {
            return;
        }
        Users receiver = request.getUser();
        if (receiver.getId() != null && receiver.getId().equals(actor.getId())) {
            return;
        }

        String actorName = actor.getFullName() == null ? "Nguoi dung" : actor.getFullName();
        String requestSubject = request.getSubject() == null ? "bai viet" : request.getSubject();

        upsertForumNotification(
                "VOTE_FORUM_POST_NOTIFICATION",
                "Tuong tac moi",
                actorName + " da tha cam xuc " + reactionType.name() + " cho bai viet \"" + requestSubject + "\"",
                request.getId(),
                receiver
        );
    }

    private void upsertForumNotification(String notificationType,
                                         String title,
                                         String content,
                                         String referenceId,
                                         Users receiver) {
        if (receiver == null || receiver.getId() == null || receiver.getId().isBlank()) {
            return;
        }

        Notification notification = notificationRepo.findByNotificationType(notificationType)
                .orElseGet(Notification::new);

        if (notification.getId() == null || notification.getId().isBlank()) {
            notification.setId("NTF_" + UUID.randomUUID());
        }

        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setCreateAt(LocalDate.now());

        List<Users> receivers = notification.getUsers();
        if (receivers == null) {
            receivers = new ArrayList<>();
            notification.setUsers(receivers);
        }

        boolean exists = receivers.stream()
                .anyMatch(user -> user != null && receiver.getId().equals(user.getId()));
        if (!exists) {
            receivers.add(receiver);
        }

        notificationRepo.save(notification);
    }


}
