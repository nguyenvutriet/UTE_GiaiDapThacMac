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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class RoleNotificationController {
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("(REQ_[A-Za-z0-9_]+)");

    private final UsersService usersService;
    private final NotificationService notificationService;

    public RoleNotificationController(UsersService usersService, NotificationService notificationService) {
        this.usersService = usersService;
        this.notificationService = notificationService;
    }

    @GetMapping("/staff/notifications")
    public String showStaffNotifications(@RequestParam(value = "tab", defaultValue = "all") String tab,
                                         @RequestParam(value = "filter", defaultValue = "all") String filter,
                                         Authentication authentication,
                                         Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        populateNotificationModel(model, user, tab, filter);
        model.addAttribute("staff", user);
        return "staff/staff-notification";
    }

    @GetMapping("/staff/notifications/{id}")
    public String showStaffNotificationDetail(@PathVariable("id") String notificationId,
                                              Authentication authentication,
                                              Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        Notification notification = notificationService.getByIdForUser(notificationId, user.getId())
                .orElse(null);
        if (notification == null) {
            return "redirect:/staff/notifications";
        }

        notificationService.markAsRead(notification);
        if ("forum".equals(tabByType(notification.getNotificationType()))) {
            String postId = resolveForumPostId(notification);
            if (postId != null && !postId.isBlank()) {
                return "redirect:/api/forum/view?openPostId=" + postId;
            }
            return "redirect:/api/forum/view";
        }

        model.addAttribute("staff", user);
        model.addAttribute("item", toItem(notification));
        return "staff/staff-notification-detail";
    }

    @GetMapping("/admin/notifications")
    public String showAdminNotifications(@RequestParam(value = "tab", defaultValue = "all") String tab,
                                         @RequestParam(value = "filter", defaultValue = "all") String filter,
                                         Authentication authentication,
                                         Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        populateNotificationModel(model, user, tab, filter);
        model.addAttribute("admin", user);
        return "admin/admin-notification";
    }

    @GetMapping("/admin/notifications/{id}")
    public String showAdminNotificationDetail(@PathVariable("id") String notificationId,
                                              Authentication authentication,
                                              Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        Notification notification = notificationService.getByIdForUser(notificationId, user.getId())
                .orElse(null);
        if (notification == null) {
            return "redirect:/admin/notifications";
        }

        notificationService.markAsRead(notification);
        if ("forum".equals(tabByType(notification.getNotificationType()))) {
            String postId = resolveForumPostId(notification);
            if (postId != null && !postId.isBlank()) {
                return "redirect:/api/forum/view?openPostId=" + postId;
            }
            return "redirect:/api/forum/view";
        }

        model.addAttribute("admin", user);
        model.addAttribute("item", toItem(notification));
        return "admin/admin-notification-detail";
    }

    private void populateNotificationModel(Model model, Users user, String tab, String filter) {
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

        model.addAttribute("activeTab", normalizeTab(tab));
        model.addAttribute("activeFilter", normalizeFilter(filter));
        model.addAttribute("notifications", displayedItems);
        model.addAttribute("countAll", allItems.size());
        model.addAttribute("countFeedback", allItems.stream().filter(item -> "feedback".equals(item.tabKey())).count());
        model.addAttribute("countForum", allItems.stream().filter(item -> "forum".equals(item.tabKey())).count());
    }

    private NotificationItem toItem(Notification notification) {
        String tabKey = tabByType(notification.getNotificationType());
        String icon = iconByType(notification.getNotificationType());
        String iconClass = iconClassByType(notification.getNotificationType());
        String title = notification.getTitle() == null || notification.getTitle().isBlank()
                ? "Thong bao"
                : notification.getTitle();
        String content = notification.getContent() == null ? "" : notification.getContent();
        String timeLabel = humanTime(notification.getCreateAt());
        boolean isRead = Boolean.TRUE.equals(notification.getRead());
        return new NotificationItem(notification.getId(), title, content, timeLabel, icon, iconClass, tabKey, isRead);
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
            return "!";
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

    private String humanTime(LocalDate createdDate) {
        if (createdDate == null) {
            return "vua xong";
        }

        long days = ChronoUnit.DAYS.between(createdDate, LocalDate.now());
        if (days <= 0) {
            return "hom nay";
        }
        if (days == 1) {
            return "1 ngay";
        }
        if (days < 30) {
            return days + " ngay";
        }
        if (days < 365) {
            return (days / 30) + " thang";
        }
        return (days / 365) + " nam";
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

    private String resolveForumPostId(Notification notification) {
        if (notification == null) {
            return null;
        }

        String ref = notification.getReferenceId();
        if (ref != null && !ref.isBlank()) {
            return ref.trim();
        }

        String source = (notification.getContent() == null ? "" : notification.getContent())
                + " "
                + (notification.getTitle() == null ? "" : notification.getTitle());
        Matcher matcher = REQUEST_ID_PATTERN.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
                                    boolean read) {
    }
}

