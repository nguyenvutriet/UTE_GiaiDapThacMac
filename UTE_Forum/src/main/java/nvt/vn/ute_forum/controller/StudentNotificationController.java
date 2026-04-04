package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.service.NotificationService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Controller
public class StudentNotificationController {

    private final UsersService usersService;
    private final NotificationService notificationService;
    private final CommentRepo commentRepo;

    public StudentNotificationController(UsersService usersService,
                                         NotificationService notificationService,
                                         CommentRepo commentRepo) {
        this.usersService = usersService;
        this.notificationService = notificationService;
        this.commentRepo = commentRepo;
    }

    @GetMapping("/api/notifications")
    public String showNotifications(@RequestParam(value = "tab", defaultValue = "all") String tab,
                                    @RequestParam(value = "filter", defaultValue = "all") String filter,
                                    Authentication authentication,
                                    Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        List<NotificationItem> allItems = notificationService.getByUserIdWithForumData(user.getId())
                .stream()
                .map(this::toItem)
                .toList();

        List<NotificationItem> byTab = allItems.stream()
                .filter(item -> matchesTab(item, tab))
                .toList();

        List<NotificationItem> displayedItems = byTab.stream()
                .filter(item -> matchesReadFilter(item, filter))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("roleLabel", "Sinh vien");
        model.addAttribute("activeTab", normalizeTab(tab));
        model.addAttribute("activeFilter", normalizeFilter(filter));
        model.addAttribute("notifications", displayedItems);
        model.addAttribute("countAll", allItems.size());
        model.addAttribute("countFeedback", allItems.stream().filter(item -> "feedback".equals(item.tabKey())).count());
        model.addAttribute("countForum", allItems.stream().filter(item -> "forum".equals(item.tabKey())).count());
        return "student/notification";
    }

    @PostMapping("/api/notifications/delete")
    public String deleteNotification(@RequestParam("notificationId") String notificationId,
                                     @RequestParam(value = "tab", defaultValue = "all") String tab,
                                     @RequestParam(value = "filter", defaultValue = "all") String filter,
                                     Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        notificationService.deleteForUser(notificationId, user.getId());
        return "redirect:/api/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
    }

    @PostMapping("/api/notifications/read")
    public String markNotificationAsRead(@RequestParam("notificationId") String notificationId,
                                         @RequestParam(value = "tab", defaultValue = "all") String tab,
                                         @RequestParam(value = "filter", defaultValue = "all") String filter,
                                         Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        notificationService.markAsReadForUser(notificationId, user.getId());
        return "redirect:/api/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
    }

    private NotificationItem toItem(Notification notification) {
        String tabKey = tabByType(notification.getNotificationType());
        String icon = iconByType(notification.getNotificationType());
        String iconClass = iconClassByType(notification.getNotificationType());
        String title = notification.getTitle() == null || notification.getTitle().isBlank()
                ? "Thông báo"
                : notification.getTitle();
        String content = notification.getContent() == null ? "" : notification.getContent();
        String timeLabel = humanTime(notification.getCreateAt());
        boolean isRead = Boolean.TRUE.equals(notification.getRead());
        String requestId = extractRequestId(notification.getId());
        return new NotificationItem(notification.getId(), title, content, timeLabel, icon, iconClass, tabKey, isRead, requestId);
    }

    private String extractRequestId(String notificationId) {
        if (notificationId == null || notificationId.isBlank()) {
            return null;
        }

        // Synthetic IDs from NotificationService:
        // VOTE_POST_<actorUserId>_<requestId>
        // COMMENT_POST_<commentId>
        // VOTE_COMMENT_<actorUserId>_<commentId>
        if (notificationId.startsWith("VOTE_POST_")) {
            int reqMarker = notificationId.lastIndexOf("_REQ_");
            return reqMarker >= 0 ? notificationId.substring(reqMarker + 1) : null;
        }

        if (notificationId.startsWith("COMMENT_POST_")) {
            String commentId = notificationId.substring("COMMENT_POST_".length());
            return commentRepo.findById(commentId)
                    .map(comment -> comment.getRequest() != null ? comment.getRequest().getId() : null)
                    .orElse(null);
        }

        if (notificationId.startsWith("VOTE_COMMENT_")) {
            int cmtMarker = notificationId.lastIndexOf("_CMT_");
            if (cmtMarker < 0) {
                return null;
            }
            String commentId = notificationId.substring(cmtMarker + 1);
            return commentRepo.findById(commentId)
                    .map(comment -> comment.getRequest() != null ? comment.getRequest().getId() : null)
                    .orElse(null);
        }

        return null;
    }

    private String tabByType(String type) {
        String normalized = normalizeType(type);
        if (normalized.contains("FEEDBACK") || normalized.contains("REPORT")) {
            return "feedback";
        }
        if (normalized.contains("FORUM") || normalized.contains("COMMENT") || normalized.contains("VOTE")) {
            return "forum";
        }
        return "all";
    }

    private String iconByType(String type) {
        String tab = tabByType(type);
        if ("feedback".equals(tab)) {
            return "✓";
        }
        if ("forum".equals(tab)) {
            return "💬";
        }
        return "✉";
    }

    private String iconClassByType(String type) {
        String normalized = normalizeType(type);
        if (normalized.contains("RESOLVED") || normalized.contains("APPROVED")) {
            return "icon-success";
        }
        if (normalized.contains("PROCESSING") || normalized.contains("FORWARDED")) {
            return "icon-processing";
        }
        if (normalized.contains("COMMENT") || normalized.contains("VOTE") || normalized.contains("FORUM")) {
            return "icon-forum";
        }
        return "icon-default";
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    private String humanTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "vừa xong";
        }

        long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (days <= 0) {
            return "hôm nay";
        }
        if (days == 1) {
            return "1 ngày";
        }
        if (days < 30) {
            return days + " ngày";
        }
        if (days < 365) {
            return (days / 30) + " tháng";
        }
        return (days / 365) + " năm";
    }

    private boolean matchesTab(NotificationItem item, String tab) {
        String normalized = normalizeTab(tab);
        if ("all".equals(normalized)) {
            return true;
        }
        return normalized.equals(item.tabKey());
    }

    private boolean matchesReadFilter(NotificationItem item, String filter) {
        String normalized = normalizeFilter(filter);
        if ("all".equals(normalized)) {
            return true;
        }
        if ("read".equals(normalized)) {
            return item.read();
        }
        return !item.read();
    }

    private String normalizeTab(String tab) {
        if (tab == null) {
            return "all";
        }
        String normalized = tab.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "feedback", "forum" -> normalized;
            default -> "all";
        };
    }

    private String normalizeFilter(String filter) {
        if (filter == null) {
            return "all";
        }
        String normalized = filter.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "read", "unread" -> normalized;
            default -> "all";
        };
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

    private record NotificationItem(String id,
                                    String title,
                                    String content,
                                    String timeLabel,
                                    String icon,
                                    String iconClass,
                                    String tabKey,
                                    boolean read,
                                    String requestId) {
    }
}


