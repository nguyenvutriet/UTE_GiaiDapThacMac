package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.NotificationService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Controller
public class StudentNotificationController {

    private final UsersService usersService;
    private final NotificationService notificationService;

    public StudentNotificationController(UsersService usersService, NotificationService notificationService) {
        this.usersService = usersService;
        this.notificationService = notificationService;
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

        List<NotificationItem> allItems = notificationService.getByUserId(user.getId())
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
        return new NotificationItem(title, content, timeLabel, icon, iconClass, tabKey, isRead);
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

    private record NotificationItem(String title,
                                    String content,
                                    String timeLabel,
                                    String icon,
                                    String iconClass,
                                    String tabKey,
                                    boolean read) {
    }
}


