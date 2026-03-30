package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.ReactionType;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.NotificationRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import nvt.vn.ute_forum.repository.VoteRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private VoteRepo voteRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private VoteCommentRepo voteCommentRepo;

    @Autowired
    private UsersRepo usersRepo;

    public List<Notification> getByUserId(String userId) {
        return notificationRepo.findByUsers_IdOrderByCreateAtDesc(userId);
    }

    public List<Notification> getByUserIdWithForumData(String userId) {
        List<Notification> stored = getByUserId(userId);
        Set<String> readMarkerIds = stored.stream()
                .filter(this::isReadMarker)
                .map(Notification::getId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        List<Notification> merged = new ArrayList<>(stored.stream().filter(item -> !isReadMarker(item)).toList());
        merged.addAll(loadVotePostNotifications(userId));
        merged.addAll(loadCommentNotifications(userId));
        merged.addAll(loadVoteCommentNotifications(userId));

        merged.forEach(item -> {
            if (readMarkerIds.contains(item.getId())) {
                item.setRead(true);
            }
        });

        List<Notification> sorted = merged.stream()
                .sorted(Comparator.comparing(Notification::getCreateAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        LinkedHashMap<String, Notification> distinct = new LinkedHashMap<>();
        for (Notification item : sorted) {
            Notification existing = distinct.get(item.getId());
            if (existing == null) {
                distinct.put(item.getId(), item);
                continue;
            }

            if (!Boolean.TRUE.equals(existing.getRead()) && Boolean.TRUE.equals(item.getRead())) {
                existing.setRead(true);
            }
        }

        return new ArrayList<>(distinct.values());
    }

    public long countUnreadByUserId(String userId) {
        return getByUserIdWithForumData(userId)
                .stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getRead()))
                .count();
    }

    @Transactional
    public boolean deleteForUser(String notificationId, String userId) {
        if (notificationId == null || notificationId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        if (!notificationRepo.existsByIdAndUsers_Id(notificationId, userId)) {
            return false;
        }

        int affectedRows = notificationRepo.unlinkNotificationFromUser(notificationId, userId);
        if (affectedRows <= 0) {
            return false;
        }

        if (notificationRepo.countReceivers(notificationId) == 0) {
            notificationRepo.deleteById(notificationId);
        }

        return true;
    }

    @Transactional
    public boolean markAsReadForUser(String notificationId, String userId) {
        if (notificationId == null || notificationId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        int updated = notificationRepo.markAsReadForUser(notificationId, userId);
        if (updated > 0) {
            return true;
        }

        if (notificationRepo.existsById(notificationId)) {
            return false;
        }

        Users user = usersRepo.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Read marker for synthetic notifications loaded from vote/comment tables.
        Notification marker = new Notification();
        marker.setId(notificationId);
        marker.setTitle("READ_MARKER");
        marker.setContent("READ_MARKER");
        marker.setNotificationType("ADMIN_NOTIFICATION");
        marker.setRead(true);
        marker.setCreateAt(LocalDateTime.now());
        marker.setUsers(List.of(user));
        notificationRepo.save(marker);
        return true;
    }

    private List<Notification> loadVotePostNotifications(String ownerUserId) {
        return voteRepo.findVoteNotificationDataByOwnerId(ownerUserId).stream()
                .map(row -> {
                    String requestId = toStringValue(row[0]);
                    String actorUserId = toStringValue(row[1]);
                    String actorName = safeActorName(toStringValue(row[2]));
                    String reaction = toReactionLabel(row[3]);
                    LocalDateTime createdAt = toDateTime(row[4]);

                    return new Notification(
                            "VOTE_POST_" + actorUserId + "_" + requestId,
                            actorName + " da tha " + reaction + " cho bai viet cua ban.",
                            "VOTE_FORUM_POST_NOTIFICATION",
                            "Bai viet cua ban vua co tuong tac",
                            false,
                            createdAt
                    );
                })
                .toList();
    }

    private List<Notification> loadCommentNotifications(String ownerUserId) {
        return commentRepo.findCommentNotificationDataByOwnerId(ownerUserId).stream()
                .map(row -> {
                    String commentId = toStringValue(row[0]);
                    String actorName = safeActorName(toStringValue(row[3]));
                    LocalDateTime createdAt = toDateTime(row[4]);

                    return new Notification(
                            "COMMENT_POST_" + commentId,
                            actorName + " da binh luan vao bai viet cua ban.",
                            "COMMENT_FORUM_POST_NOTIFICATION",
                            "Bai viet cua ban co binh luan moi",
                            false,
                            createdAt
                    );
                })
                .toList();
    }

    private List<Notification> loadVoteCommentNotifications(String ownerUserId) {
        return voteCommentRepo.findVoteCommentNotificationDataByOwnerId(ownerUserId).stream()
                .map(row -> {
                    String commentId = toStringValue(row[0]);
                    String actorUserId = toStringValue(row[2]);
                    String actorName = safeActorName(toStringValue(row[3]));
                    String reaction = toReactionLabel(row[4]);
                    LocalDateTime createdAt = toDateTime(row[5]);

                    return new Notification(
                            "VOTE_COMMENT_" + actorUserId + "_" + commentId,
                            actorName + " da tha " + reaction + " cho mot binh luan trong bai viet cua ban.",
                            "VOTE_FORUM_POST_NOTIFICATION",
                            "Binh luan trong bai viet cua ban co tuong tac moi",
                            false,
                            createdAt
                    );
                })
                .toList();
    }

    private String safeActorName(String name) {
        return (name == null || name.isBlank()) ? "Nguoi dung" : name;
    }

    private String toStringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String toReactionLabel(Object value) {
        if (value instanceof ReactionType reactionType) {
            return reactionType.name();
        }
        return value == null ? "LIKE" : value.toString();
    }

    private LocalDateTime toDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        }
        return LocalDateTime.now();
    }

    private long safeEpoch(LocalDateTime dateTime) {
        return dateTime == null ? 0L : dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    private boolean isReadMarker(Notification notification) {
        if (notification == null) {
            return false;
        }

        String title = notification.getTitle() == null ? "" : notification.getTitle().trim();
        String content = notification.getContent() == null ? "" : notification.getContent().trim();

        if ("READ_MARKER".equals(title) && "READ_MARKER".equals(content)) {
            return true;
        }

        // Backward-compatible cleanup for old marker rows created earlier.
        String normalizedTitle = title
                .toLowerCase(java.util.Locale.ROOT)
                .replace("ô", "o")
                .replace("ơ", "o")
                .replace("ă", "a")
                .replace("â", "a")
                .replace("á", "a")
                .replace("à", "a")
                .replace("ả", "a")
                .replace("ã", "a")
                .replace("ạ", "a")
                .replace("đ", "d");

        String normalizedContent = content
                .toLowerCase(java.util.Locale.ROOT)
                .replace("ọ", "o")
                .replace("ó", "o")
                .replace("ò", "o")
                .replace("ỏ", "o")
                .replace("õ", "o")
                .replace("đ", "d");

        return normalizedTitle.contains("thong bao") && normalizedContent.startsWith("da doc");
    }
}
