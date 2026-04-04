package nvt.vn.ute_forum.controller.student;


import nvt.vn.ute_forum.model.ForwardingLog;
import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.RequestStatusHistory;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.ForwardingLogService;
import nvt.vn.ute_forum.service.MessageService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.RequestStatusHistoryService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SubmittedFeedbackHistoryController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int HISTORY_PAGE_SIZE = 9;

    enum RequestStatusEnum {
        PENDING("Đang chờ tiếp nhận", "pending", "⏳"),
        PROCESSING("Đang xử lý", "approved", "⚙️"),
        FORWARDING("Đã được chuyển tiếp", "forwarding", "🔁"),
        APPROVED("Đang xử lý", "approved", "⚙️"),
        RESOLVED("Đã xử lý", "done", "✅"),
        REJECTED("Từ chối", "rejected", "⛔");

        private final String label;
        private final String cssClass;
        private final String icon;

        RequestStatusEnum(String label, String cssClass, String icon) {
            this.label = label;
            this.cssClass = cssClass;
            this.icon = icon;
        }

        public String getLabel() { return label; }
        public String getCssClass() { return cssClass; }
        public String getIcon() { return icon; }

        public static RequestStatusEnum fromStatus(String status) {
            if (status == null || status.isBlank()) {
                return PENDING;
            }
            try {
                return RequestStatusEnum.valueOf(status.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return PENDING;
            }
        }
    }

    @Autowired
    private RequestService requestService;
    @Autowired
    private ForwardingLogService forwardingLogService;
    @Autowired
    private RequestStatusHistoryService requestStatusHistoryService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private ClarificationConversationService clarificationConversationService;
    @Autowired
    private MessageService messageService;

    @GetMapping("/api/history")
    public String show(@RequestParam(value = "requestId", required = false) String requestId,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "departmentId", required = false) String departmentId,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "categoryId", required = false) String categoryId,
                       @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                       Authentication authentication,
                       Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        List<Request> allRequests = requestService.getRequestsByUserId(user.getId());
        List<Request> filteredRequests = requestService.filterStudentRequests(allRequests, keyword, departmentId, status, categoryId);
        Request selectedRequest = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
        int totalItems = filteredRequests.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / HISTORY_PAGE_SIZE));
        int currentPage = page == null ? 1 : page;
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        List<Request> requests = filteredRequests;
        if (selectedRequest == null) {
            int fromIndex = (currentPage - 1) * HISTORY_PAGE_SIZE;
            int toIndex = Math.min(fromIndex + HISTORY_PAGE_SIZE, totalItems);
            requests = filteredRequests.subList(fromIndex, toIndex);
        }
        List<ForwardingLog> forwardingLogs = selectedRequest == null
                ? new ArrayList<>()
                : forwardingLogService.getByRequestId(selectedRequest.getId());
        List<RequestStatusHistory> statusHistories = selectedRequest == null
                ? new ArrayList<>()
                : requestStatusHistoryService.getByRequestId(selectedRequest.getId());
        List<OpenConversationItem> openConversations = selectedRequest == null
                ? new ArrayList<>()
                : buildOpenConversations(user.getId(), selectedRequest.getId());
        boolean chatEnabled = !openConversations.isEmpty();
        ClarificationConversation selectedConversation = chatEnabled
                ? clarificationConversationService.findByRequestForStudent(selectedRequest.getId(), user.getId())
                .stream()
                .findFirst()
                .orElse(null)
                : null;
        List<MessageService.ChatMessageView> conversationMessages = (chatEnabled && selectedConversation != null)
                ? messageService.getConversationMessages(selectedConversation.getId(), user.getId())
                : new ArrayList<>();

        model.addAttribute("user", user);
        model.addAttribute("roleLabel", "Sinh vien");
        model.addAttribute("requests", requests);
        model.addAttribute("selectedRequest", selectedRequest);
        model.addAttribute("selectedRequestId", selectedRequest == null ? "" : safeValue(selectedRequest.getId()));
        model.addAttribute("selectedTimeline", buildTimeline(selectedRequest, statusHistories, forwardingLogs));
        model.addAttribute("currentHandlingDepartment", requestService.resolveCurrentDepartment(selectedRequest, forwardingLogs));
        model.addAttribute("chatEnabled", chatEnabled);
        model.addAttribute("chatConversationId", (chatEnabled && selectedConversation != null) ? selectedConversation.getId() : "");
        model.addAttribute("chatMessages", conversationMessages);
        model.addAttribute("openConversations", openConversations);
        model.addAttribute("keyword", safeValue(keyword));
        model.addAttribute("departmentId", safeValue(departmentId));
        model.addAttribute("status", safeValue(status));
        model.addAttribute("categoryId", safeValue(categoryId));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);
        model.addAttribute("departmentOptions", buildOptionItems(requestService.buildDepartmentOptionMap(allRequests)));
        model.addAttribute("categoryOptions", buildOptionItems(requestService.buildCategoryOptionMap(allRequests)));
        model.addAttribute("statusOptions", buildOptionItems(requestService.buildStatusOptionMap(allRequests)));

        return "student/submitted-feedback-history";
    }


    private List<OpenConversationItem> buildOpenConversations(String userId, String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return new ArrayList<>();
        }

        List<ClarificationConversation> conversations = clarificationConversationService
                .findByRequestForStudent(requestId, userId);

        if (conversations.isEmpty()) {
            return new ArrayList<>();
        }

        List<OpenConversationItem> items = new ArrayList<>();
        for (ClarificationConversation conversation : conversations) {
            Request request = conversation.getRequest();
            String subject = safeValue(conversation.getSubject());
            if (subject.isBlank() && request != null) {
                subject = safeValue(request.getSubject());
            }
            if (subject.isBlank()) {
                subject = "Trao đổi";
            }
            String resolvedRequestId = request == null ? "" : safeValue(request.getId());
            String dateLabel = conversation.getCreateAt() == null
                    ? ""
                    : conversation.getCreateAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            List<MessageService.ChatMessageView> messages = messageService.getConversationMessages(conversation.getId(), userId);

            String preview = messages.stream()
                    .reduce((first, second) -> second)
                    .map(last -> {
                        String text = safeValue(last.text()).trim();
                        if (!text.isBlank()) {
                            return text;
                        }
                        return "Nhấn để xem chi tiết cuộc trao đổi...";
                    })
                    .orElse("Nhấn để xem chi tiết cuộc trao đổi...");

            boolean open = Boolean.TRUE.equals(conversation.getOpen());
            items.add(new OpenConversationItem(
                    safeValue(conversation.getId()),
                    resolvedRequestId,
                    subject,
                    dateLabel,
                    preview,
                    open ? "Đang mở" : "Đã đóng",
                    open
            ));
        }

        return items;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
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

    private List<TimelineItem> buildTimeline(Request selectedRequest,
                                             List<RequestStatusHistory> statusHistories,
                                             List<ForwardingLog> forwardingLogs) {
        List<TimelineItem> timeline = new ArrayList<>();
        if (selectedRequest == null) {
            return timeline;
        }

        String handlingDepartment = safeDepartmentName(selectedRequest.getDepartment() == null ? null : selectedRequest.getDepartment().getName());
        List<ForwardingLog> sortedForwardingLogs = forwardingLogs == null
                ? Collections.emptyList()
                : forwardingLogs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ForwardingLog::getForwardAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .collect(Collectors.toList());

        for (RequestStatusHistory history : statusHistories) {
            LocalDateTime occurredAt = history.getCreateAt();
            RequestStatusEnum statusEnum = RequestStatusEnum.fromStatus(history.getStatus());
            String resolvedStatus = statusEnum.getLabel();
            String message = buildTimelineMessage(statusEnum, occurredAt, selectedRequest, sortedForwardingLogs);

            timeline.add(new TimelineItem(
                    resolvedStatus,
                    message,
                    formatDateTime(occurredAt),
                    toCssClass(history.getStatus()),
                    handlingDepartment
            ));
        }

        return timeline;
    }

    private String buildTimelineMessage(RequestStatusEnum statusEnum,
                                        LocalDateTime occurredAt,
                                        Request request,
                                        List<ForwardingLog> sortedForwardingLogs) {
        ForwardingLog matchedLog = findLatestForwardingAtOrBefore(occurredAt, sortedForwardingLogs);
        String handlingDepartmentAtTime = resolveDepartmentAt(occurredAt, request, sortedForwardingLogs);

        return switch (statusEnum) {
            case PENDING -> "Đang chờ tiếp nhận";
            case PROCESSING, APPROVED -> "Đang được " + handlingDepartmentAtTime + " xử lý.";
            case FORWARDING -> {
                if (matchedLog != null) {
                    String fromDepartment = safeDepartmentName(matchedLog.getFromdepartment() == null ? null : matchedLog.getFromdepartment().getName());
                    String toDepartment = safeDepartmentName(matchedLog.getTodepartment() == null ? null : matchedLog.getTodepartment().getName());
                    String note = safeValue(matchedLog.getNote()).trim();
                    String forwardingMessage = "Đã được chuyển tiếp đến " + toDepartment + " (từ " + fromDepartment + ")." + '\n';
                    if (!note.isBlank()) {
                        forwardingMessage += " Ghi chú: " + note + ".";
                    }
                    yield forwardingMessage;
                }
                yield "Đang được chuyển tiếp, chưa xác định phòng ban đích.";
            }
            case RESOLVED -> "Đã được " + handlingDepartmentAtTime + " giải quyết.";
            case REJECTED -> "Yêu cầu bị từ chối tại " + handlingDepartmentAtTime + ".";
        };
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "Chưa có thời gian";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private String resolveDepartmentAt(LocalDateTime occurredAt,
                                       Request request,
                                       List<ForwardingLog> sortedForwardingLogs) {
        ForwardingLog latestLog = findLatestForwardingAtOrBefore(occurredAt, sortedForwardingLogs);
        if (latestLog != null) {
            return safeDepartmentName(latestLog.getTodepartment() == null ? null : latestLog.getTodepartment().getName());
        }
        return safeDepartmentName(request.getDepartment() == null ? null : request.getDepartment().getName());
    }

    private ForwardingLog findLatestForwardingAtOrBefore(LocalDateTime occurredAt, List<ForwardingLog> sortedForwardingLogs) {
        if (occurredAt == null || sortedForwardingLogs == null || sortedForwardingLogs.isEmpty()) {
            return null;
        }

        ForwardingLog latest = null;
        for (ForwardingLog log : sortedForwardingLogs) {
            if (log.getForwardAt() == null) {
                continue;
            }
            if (!log.getForwardAt().isAfter(occurredAt)) {
                latest = log;
                continue;
            }
            break;
        }
        return latest;
    }

    private String safeDepartmentName(String name) {
        return name == null || name.isBlank() ? "Chưa xác định phòng ban" : name;
    }

    private String toCssClass(String status) {
        return RequestStatusEnum.fromStatus(status).getCssClass();
    }

    public static class TimelineItem {
        private final String status;
        private final String message;
        private final String time;
        private final String cssClass;
        private final String department;

        public TimelineItem(String status, String message, String time, String cssClass, String department) {
            this.status = status;
            this.message = message;
            this.time = time;
            this.cssClass = cssClass;
            this.department = department;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getTime() {
            return time;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getDepartment() {
            return department;
        }
    }

    public static class OptionItem {
        private final String value;
        private final String label;

        public OptionItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    private List<OptionItem> buildOptionItems(Map<String, String> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.entrySet().stream()
                .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OptionItem::getLabel))
                .collect(Collectors.toList());
    }

    public static class OpenConversationItem {
        private final String conversationId;
        private final String requestId;
        private final String subject;
        private final String dateLabel;
        private final String preview;
        private final String statusLabel;
        private final boolean open;

        public OpenConversationItem(String conversationId,
                                    String requestId,
                                    String subject,
                                    String dateLabel,
                                    String preview,
                                    String statusLabel,
                                    boolean open) {
            this.conversationId = conversationId;
            this.requestId = requestId;
            this.subject = subject;
            this.dateLabel = dateLabel;
            this.preview = preview;
            this.statusLabel = statusLabel;
            this.open = open;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getSubject() {
            return subject;
        }

        public String getDateLabel() {
            return dateLabel;
        }

        public String getPreview() {
            return preview;
        }

        public String getStatusLabel() {
            return statusLabel;
        }

        public boolean isOpen() {
            return open;
        }
    }
}
