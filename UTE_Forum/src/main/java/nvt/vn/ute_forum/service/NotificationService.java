package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public void notifyNewAnnouncement(Announcement announcement, List<Users> receivers) {
        if (announcement == null) {
            return;
        }

        List<Users> distinctReceivers = distinctUsers(receivers);
        if (distinctReceivers.isEmpty()) {
            return;
        }

        String title = (announcement.getTitle() == null || announcement.getTitle().isBlank())
                ? "Thong bao moi"
                : announcement.getTitle().trim();

        String content = "Co thong bao moi" + (announcement.getTitle() == null || announcement.getTitle().isBlank()
                ? "."
                : ": \"" + announcement.getTitle().trim() + "\".");

        upsertAnnouncementNotification(
                "NEW_ANNOUNCEMENT_NOTIFICATION",
                title,
                content,
                announcement.getId(),
                distinctReceivers
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

    private void upsertAnnouncementNotification(String notificationType,
                                                String title,
                                                String content,
                                                String referenceId,
                                                List<Users> receivers) {
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
        notification.setUsers(new ArrayList<>(receivers));

        notificationRepo.save(notification);
    }

    private List<Users> distinctUsers(List<Users> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, Users> deduplicated = new LinkedHashMap<>();
        for (Users user : users) {
            if (user != null && user.getId() != null && !user.getId().isBlank()) {
                deduplicated.putIfAbsent(user.getId(), user);
            }
        }
        return new ArrayList<>(deduplicated.values());
    }


}
