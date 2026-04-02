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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Controller
public class RoleNotificationController {

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

    @PostMapping("/staff/notifications/delete")
    public String deleteStaffNotification(@RequestParam("notificationId") String notificationId,
                                          @RequestParam(value = "tab", defaultValue = "all") String tab,
                                          @RequestParam(value = "filter", defaultValue = "all") String filter,
                                          Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        notificationService.deleteForUser(notificationId, user.getId());
        return "redirect:/staff/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
    }

    @PostMapping("/staff/notifications/read")
    public String markStaffNotificationAsRead(@RequestParam("notificationId") String notificationId,
                                              @RequestParam(value = "tab", defaultValue = "all") String tab,
                                              @RequestParam(value = "filter", defaultValue = "all") String filter,
                                              Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        notificationService.markAsReadForUser(notificationId, user.getId());
        return "redirect:/staff/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
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

    @PostMapping("/admin/notifications/delete")
    public String deleteAdminNotification(@RequestParam("notificationId") String notificationId,
                                          @RequestParam(value = "tab", defaultValue = "all") String tab,
                                          @RequestParam(value = "filter", defaultValue = "all") String filter,
                                          Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        notificationService.deleteForUser(notificationId, user.getId());
        return "redirect:/admin/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
    }

    @PostMapping("/admin/notifications/read")
    public String markAdminNotificationAsRead(@RequestParam("notificationId") String notificationId,
                                              @RequestParam(value = "tab", defaultValue = "all") String tab,
                                              @RequestParam(value = "filter", defaultValue = "all") String filter,
                                              Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/api/forum/view";
        }

        notificationService.markAsReadForUser(notificationId, user.getId());
        return "redirect:/admin/notifications?tab=" + normalizeTab(tab) + "&filter=" + normalizeFilter(filter);
    }

    private void populateNotificationModel(Model model, Users user, String tab, String filter) {
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

        model.addAttribute("activeTab", normalizeTab(tab));
        model.addAttribute("activeFilter", normalizeFilter(filter));
        model.addAttribute("notifications", displayedItems);
        model.addAttribute("countAll", allItems.size());
        model.addAttribute("countFeedback", allItems.stream().filter(item -> "feedback".equals(item.tabKey())).count());
        model.addAttribute("countForum", allItems.stream().filter(item -> "forum".equals(item.tabKey())).count());
    }

    private NotificationItem toItem(Notification notification) {
        String signal = normalizeSignal(notification);
        String tabKey = tabBySignal(signal);
        String icon = iconBySignal(signal);
        String iconClass = iconClassBySignal(signal);
        String title = notification.getTitle() == null || notification.getTitle().isBlank()
                ? "Thông báo"
                : beautifyVietnamese(notification.getTitle());
        String content = notification.getContent() == null ? "" : beautifyVietnamese(notification.getContent());
        String timeLabel = humanTime(notification.getCreateAt());
        boolean isRead = Boolean.TRUE.equals(notification.getRead());
        return new NotificationItem(notification.getId(), title, content, timeLabel, icon, iconClass, tabKey, isRead);
    }

    private String beautifyVietnamese(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String text = input;
        text = text.replaceAll("(?i)\\bGop y\\b", "Góp ý");
        text = text.replaceAll("(?i)\\bThong bao\\b", "Thông báo");
        text = text.replaceAll("(?i)\\bDien dan\\b", "Diễn đàn");
        text = text.replaceAll("(?i)\\bBinh luan\\b", "Bình luận");
        text = text.replaceAll("(?i)\\bBao cao\\b", "Báo cáo");
        text = text.replaceAll("(?i)\\bHoan tat\\b", "Hoàn tất");
        text = text.replaceAll("(?i)\\bXu ly\\b", "Xử lý");
        text = text.replaceAll("(?i)\\bthanh cong\\b", "thành công");
        text = text.replaceAll("(?i)\\bda\\b", "đã");
        text = text.replaceAll("(?i)\\bduoc\\b", "được");
        text = text.replaceAll("(?i)\\bgui\\b", "gửi");
        text = text.replaceAll("(?i)\\bcua\\b", "của");
        text = text.replaceAll("(?i)\\bban\\b", "bạn");
        text = text.replaceAll("(?i)\\bmoi\\b", "mới");
        text = text.replaceAll("(?i)\\btu\\b", "từ");
        text = text.replaceAll("(?i)\\btoi\\b", "tới");
        return text;
    }

    private String tabBySignal(String signal) {
        if (containsAny(signal, "FEEDBACK", "GOP Y", "REPORT", "BAO CAO")) {
            return "feedback";
        }
        if (containsAny(signal, "FORUM", "COMMENT", "BINH LUAN", "VOTE", "REACTION", "LIKE", "LOVE", "TIM", "THICH")) {
            return "forum";
        }
        return "all";
    }

    private String iconBySignal(String signal) {
        if (containsAny(signal, "LIKE", "LOVE", "REACTION", "VOTE", "TIM", "THICH")) {
            return "❤";
        }
        if (containsAny(signal, "COMMENT", "BINH LUAN")) {
            return "💬";
        }
        if (containsAny(signal, "FORWARD", "CHUYEN TIEP")) {
            return "🔁";
        }
        if (containsAny(signal, "RESOLVED", "APPROVED", "THANH CONG", "DA XU LY")) {
            return "✅";
        }
        if (containsAny(signal, "FEEDBACK", "GOP Y")) {
            return "📝";
        }
        if (containsAny(signal, "REPORT", "BAO CAO")) {
            return "🚩";
        }
        if (containsAny(signal, "ANNOUNCE", "THONG BAO")) {
            return "📢";
        }
        return "🔔";
    }

    private String iconClassBySignal(String signal) {
        if (containsAny(signal, "RESOLVED", "APPROVED", "THANH CONG", "DA XU LY")) {
            return "icon-success";
        }
        if (containsAny(signal, "PROCESSING", "FORWARDED", "FORWARD", "CHUYEN TIEP")) {
            return "icon-processing";
        }
        if (containsAny(signal, "COMMENT", "VOTE", "FORUM", "REACTION", "LIKE", "LOVE", "TIM", "THICH")) {
            return "icon-forum";
        }
        return "icon-default";
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSignal(Notification notification) {
        String merged = String.join(" ",
                safe(notification.getNotificationType()),
                safe(notification.getTitle()),
                safe(notification.getContent())
        );

        // Remove accents so matching works for both "thông báo" and "thong bao".
        String noAccent = Normalizer.normalize(merged, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return noAccent.toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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
                                    boolean read) {
    }
}

