package nvt.vn.ute_forum.controller;


import nvt.vn.ute_forum.model.ForwardingLog;
import nvt.vn.ute_forum.model.Category;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.RequestStatusHistory;
import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.ForwardingLogService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.RequestStatusHistoryService;
import nvt.vn.ute_forum.service.UsersService;
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

    private final RequestService requestService;
    private final ForwardingLogService forwardingLogService;
    private final RequestStatusHistoryService requestStatusHistoryService;
    private final UsersService usersService;

    public SubmittedFeedbackHistoryController(RequestService requestService,
                                              ForwardingLogService forwardingLogService,
                                              RequestStatusHistoryService requestStatusHistoryService,
                                              UsersService usersService) {
        this.requestService = requestService;
        this.forwardingLogService = forwardingLogService;
        this.requestStatusHistoryService = requestStatusHistoryService;
        this.usersService = usersService;
    }

    @GetMapping("/api/history")
    public String show(@RequestParam(value = "requestId", required = false) String requestId,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "departmentId", required = false) String departmentId,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "categoryId", required = false) String categoryId,
                       Authentication authentication,
                       Model model) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        List<Request> allRequests = requestService.getRequestsByUserId(user.getId());
        List<Request> requests = applyFilters(allRequests, keyword, departmentId, status, categoryId);
        Request selectedRequest = requestService.getRequestByIdAndUserId(requestId, user.getId()).orElse(null);
        List<ForwardingLog> forwardingLogs = selectedRequest == null
                ? new ArrayList<>()
                : forwardingLogService.getByRequestId(selectedRequest.getId());
        List<RequestStatusHistory> statusHistories = selectedRequest == null
                ? new ArrayList<>()
                : requestStatusHistoryService.getByRequestId(selectedRequest.getId());

        model.addAttribute("user", user);
        model.addAttribute("roleLabel", "Sinh vien");
        model.addAttribute("requests", requests);
        model.addAttribute("selectedRequest", selectedRequest);
        model.addAttribute("selectedTimeline", buildTimeline(selectedRequest, statusHistories, forwardingLogs));
        model.addAttribute("currentHandlingDepartment", resolveCurrentDepartment(selectedRequest, forwardingLogs));
        model.addAttribute("keyword", safeValue(keyword));
        model.addAttribute("departmentId", safeValue(departmentId));
        model.addAttribute("status", safeValue(status));
        model.addAttribute("categoryId", safeValue(categoryId));
        model.addAttribute("departmentOptions", buildDepartmentOptions(allRequests));
        model.addAttribute("categoryOptions", buildCategoryOptions(allRequests));
        model.addAttribute("statusOptions", buildStatusOptions(allRequests));

        return "student/submitted-feedback-history";
    }

    private List<Request> applyFilters(List<Request> source,
                                       String keyword,
                                       String departmentId,
                                       String status,
                                       String categoryId) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedKeyword = normalize(keyword);
        String normalizedDepartmentId = normalize(departmentId);
        String normalizedStatus = normalize(status);
        String normalizedCategoryId = normalize(categoryId);

        return source.stream()
                .filter(req -> normalizedKeyword.isEmpty() || containsIgnoreCase(req.getSubject(), normalizedKeyword))
                .filter(req -> normalizedDepartmentId.isEmpty() || hasDepartment(req, normalizedDepartmentId))
                .filter(req -> normalizedStatus.isEmpty() || sameStatus(req, normalizedStatus))
                .filter(req -> normalizedCategoryId.isEmpty() || hasCategory(req, normalizedCategoryId))
                .collect(Collectors.toList());
    }

    private List<OptionItem> buildDepartmentOptions(List<Request> requests) {
        Map<String, String> options = new LinkedHashMap<>();
        for (Request req : requests) {
            if (req.getDepartment() != null && req.getDepartment().getId() != null && req.getDepartment().getName() != null) {
                options.put(req.getDepartment().getId(), req.getDepartment().getName());
            }
        }
        return options.entrySet().stream()
                .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OptionItem::getLabel))
                .collect(Collectors.toList());
    }

    private List<OptionItem> buildCategoryOptions(List<Request> requests) {
        Map<String, String> options = new LinkedHashMap<>();
        for (Request req : requests) {
            if (req.getCategories() == null) {
                continue;
            }
            req.getCategories().stream()
                    .filter(Objects::nonNull)
                    .filter(cat -> cat.getId() != null && cat.getSubject() != null)
                    .forEach(cat -> options.put(cat.getId(), cat.getSubject()));
        }
        return options.entrySet().stream()
                .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OptionItem::getLabel))
                .collect(Collectors.toList());
    }

    private List<OptionItem> buildStatusOptions(List<Request> requests) {
        List<String> order = Arrays.asList("PENDING", "PROCESSING", "FORWARDING", "APPROVED", "REJECTED", "RESOLVED");
        Map<String, String> options = new LinkedHashMap<>();

        for (String key : order) {
            boolean exists = requests.stream()
                    .map(Request::getCurrentStatus)
                    .anyMatch(raw -> sameStatus(raw, key));
            if (exists) {
                options.put(key, translateStatus(key));
            }
        }

        requests.stream()
                .map(Request::getCurrentStatus)
                .filter(raw -> raw != null && !raw.isBlank())
                .forEach(raw -> options.putIfAbsent(raw.trim().toUpperCase(Locale.ROOT), translateStatus(raw)));

        return options.entrySet().stream()
                .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private boolean hasDepartment(Request request, String departmentId) {
        return request.getDepartment() != null
                && request.getDepartment().getId() != null
                && request.getDepartment().getId().equalsIgnoreCase(departmentId);
    }

    private boolean hasCategory(Request request, String categoryId) {
        if (request.getCategories() == null || request.getCategories().isEmpty()) {
            return false;
        }
        return request.getCategories().stream()
                .filter(Objects::nonNull)
                .anyMatch(cat -> cat.getId() != null && cat.getId().equalsIgnoreCase(categoryId));
    }

    private boolean sameStatus(Request request, String status) {
        return sameStatus(request.getCurrentStatus(), status);
    }

    private boolean sameStatus(String rawStatus, String statusFilter) {
        if (rawStatus == null || statusFilter == null) {
            return false;
        }
        return rawStatus.trim().equalsIgnoreCase(statusFilter.trim());
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
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
        String categoryContext = buildCategoryContext(selectedRequest);
        List<ForwardingLog> sortedForwardingLogs = forwardingLogs == null
                ? Collections.emptyList()
                : forwardingLogs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ForwardingLog::getForwardAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .collect(Collectors.toList());

        for (RequestStatusHistory history : statusHistories) {
            LocalDateTime occurredAt = history.getCreateAt();
            String rawStatus = normalizeStatus(history.getStatus());
            String resolvedStatus = translateStatus(history.getStatus());
            String message = buildTimelineMessage(rawStatus, resolvedStatus, occurredAt, selectedRequest, sortedForwardingLogs, categoryContext);

            timeline.add(new TimelineItem(
                    resolvedStatus,
                    message,
                    formatDateTime(occurredAt),
                    toCssClass(history.getStatus()),
                    handlingDepartment,
                    null,
                    "Trạng thái"
            ));
        }

        return timeline;
    }

    private String buildTimelineMessage(String rawStatus,
                                        String resolvedStatus,
                                        LocalDateTime occurredAt,
                                        Request request,
                                        List<ForwardingLog> sortedForwardingLogs,
                                        String categoryContext) {
        ForwardingLog matchedLog = findLatestForwardingAtOrBefore(occurredAt, sortedForwardingLogs);
        String handlingDepartmentAtTime = resolveDepartmentAt(occurredAt, request, sortedForwardingLogs);

        if ("PENDING".equals(rawStatus)) {
            return "Đang chờ tiếp nhận" + categoryContext + ".";
        }

        if ("FORWARDING".equals(rawStatus)) {
            if (matchedLog != null) {
                String fromDepartment = safeDepartmentName(matchedLog.getFromdepartment() == null ? null : matchedLog.getFromdepartment().getName());
                String toDepartment = safeDepartmentName(matchedLog.getTodepartment() == null ? null : matchedLog.getTodepartment().getName());
                String note = safeValue(matchedLog.getNote()).trim();
                String forwardingMessage = "Đang được chuyển tiếp đến " + toDepartment + " (từ " + fromDepartment + ").";
                if (!note.isBlank()) {
                    forwardingMessage += " Ghi chú: " + note + ".";
                }
                return forwardingMessage;
            }
            return "Đang được chuyển tiếp, chưa xác định phòng ban đích.";
        }

        if ("RESOLVED".equals(rawStatus)) {
            return "Đã được " + handlingDepartmentAtTime + " giải quyết.";
        }

        if ("PROCESSING".equals(rawStatus)) {
            return "Đang được " + handlingDepartmentAtTime + " xử lý.";
        }

        if ("APPROVED".equals(rawStatus)) {
            return "Đã được " + handlingDepartmentAtTime + " tiếp nhận và duyệt.";
        }

        if ("REJECTED".equals(rawStatus)) {
            return "Yêu cầu bị từ chối tại " + handlingDepartmentAtTime + ".";
        }

        return "Cập nhật trạng thái: " + resolvedStatus + ".";
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

    private String buildCategoryContext(Request request) {
        if (request == null || request.getCategories() == null || request.getCategories().isEmpty()) {
            return "";
        }

        String categories = request.getCategories().stream()
                .filter(Objects::nonNull)
                .map(Category::getSubject)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        return categories.isBlank() ? "" : " (Danh mục: " + categories + ")";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String translateStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Đang xử lý";
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            return "Chờ tiếp nhận";
        }
        if ("APPROVED".equalsIgnoreCase(status)) {
            return "Đã duyệt";
        }
        if ("REJECTED".equalsIgnoreCase(status)) {
            return "Từ chối";
        }
        if ("RESOLVED".equalsIgnoreCase(status)) {
            return "Đã xử lý";
        }
        if ("FORWARDING".equalsIgnoreCase(status)) {
            return "Đang chuyển tiếp";
        }
        if ("PROCESSING".equalsIgnoreCase(status)) {
            return "Đang xử lý";
        }
        return status;
    }

    private String resolveCurrentDepartment(Request selectedRequest, List<ForwardingLog> forwardingLogs) {
        if (selectedRequest == null) {
            return "Chưa xác định";
        }
        if (forwardingLogs != null && !forwardingLogs.isEmpty()) {
            ForwardingLog lastLog = forwardingLogs.get(forwardingLogs.size() - 1);
            if (lastLog.getTodepartment() != null && lastLog.getTodepartment().getName() != null) {
                return lastLog.getTodepartment().getName();
            }
        }
        if (selectedRequest.getDepartment() != null && selectedRequest.getDepartment().getName() != null) {
            return selectedRequest.getDepartment().getName();
        }
        return "Chưa xác định";
    }

    private String safeDepartmentName(String name) {
        return name == null || name.isBlank() ? "Chưa xác định phòng ban" : name;
    }

    private String toCssClass(String status) {
        if (status == null) {
            return "processing";
        }
        if ("RESOLVED".equalsIgnoreCase(status) || "Đã xử lý".equalsIgnoreCase(status)) {
            return "done";
        }
        if ("REJECTED".equalsIgnoreCase(status) || "Từ chối".equalsIgnoreCase(status)) {
            return "waiting";
        }
        if ("PROCESSING".equalsIgnoreCase(status) || "Đang xử lý".equalsIgnoreCase(status)) {
            return "processing";
        }
        return "waiting";
    }

    public static class TimelineItem {
        private final String status;
        private final String message;
        private final String time;
        private final String cssClass;
        private final String department;
        private final String fromDepartment;
        private final String typeLabel;

        public TimelineItem(String status, String message, String time, String cssClass, String department, String fromDepartment, String typeLabel) {
            this.status = status;
            this.message = message;
            this.time = time;
            this.cssClass = cssClass;
            this.department = department;
            this.fromDepartment = fromDepartment;
            this.typeLabel = typeLabel;
        }

        public TimelineItem(String status, String message, String time, String cssClass, String department, String fromDepartment) {
            this.status = status;
            this.message = message;
            this.time = time;
            this.cssClass = cssClass;
            this.department = department;
            this.fromDepartment = fromDepartment;
            this.typeLabel = null;
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

        public String getFromDepartment() {
            return fromDepartment;
        }

        public String getTypeLabel() {
            return typeLabel;
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
}
