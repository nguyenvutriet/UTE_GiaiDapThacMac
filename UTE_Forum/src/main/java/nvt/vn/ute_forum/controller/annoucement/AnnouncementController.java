package nvt.vn.ute_forum.controller.annoucement;


import nvt.vn.ute_forum.dto.AnnouncementResponse;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import nvt.vn.ute_forum.repository.NotificationRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.service.AnnoucementService;
import nvt.vn.ute_forum.service.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepo announcementRepository;

    @Autowired
    private AnnoucementService announcementService;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private FileAttachmentService fileAttachmentService;

    @Autowired
    private NotificationRepo notificationRepo;

    @GetMapping
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập nè bà ơi! 😂");
        }

        // 1. Xử lý phân trang & sắp xếp
        Sort sortOrder = sort.equals("oldest") ? Sort.by("date").ascending() : Sort.by("date").descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 2. Tạo bộ lọc (Specification)
        // Lưu ý: Trong file AnnoucementService, Predicate phải là jakarta.persistence.criteria.Predicate
        var spec = AnnoucementService.AnnouncementSpecifications.filterAnnouncements(
                departmentId,  startDate, endDate);

        // 3. Thực hiện truy vấn (Repo phải extends JpaSpecificationExecutor)
        Page<Announcement> announcementPage = announcementRepository.findAll(spec, pageable);

        // 4. Chuyển đổi sang DTO để trả về JSON cho JavaScript
        Page<AnnouncementResponse> responsePage = announcementPage.map(announcementService::mapToDTO);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchQuick(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Kiểm tra xem bà/ông nào đang gọi API
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập là không cho tìm kiếm đâu nhé! 😂");
        }

        // 2. Giới hạn số lượng kết quả gợi ý (Top 7)
        // Lưu ý: PageRequest.of(0, 7) để lấy trang đầu tiên với 7 bản ghi
        Pageable topSeven = PageRequest.of(0, 7);

        // 3. Gọi Repo với keyword đã xử lý lỗi CAST @Lob lúc nãy
        List<Announcement> results = announcementRepository.searchByKeyword(keyword, topSeven);

        // 4. Map sang DTO để trả về đúng format JSON
        List<AnnouncementResponse> dtoList = results.stream()
                .map(announcementService::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAnnouncement(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        return createAnnouncementInternal(
                payload.getOrDefault("title", ""),
                payload.getOrDefault("content", ""),
                null,
                null,
                false,
                userDetails
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAnnouncementWithAttachments(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            @RequestParam(value = "recipientDepartmentIds", required = false) List<String> recipientDepartmentIds,
            @RequestParam(value = "allDepartments", defaultValue = "false") boolean allDepartments,
            @AuthenticationPrincipal UserDetails userDetails) {

        return createAnnouncementInternal(title, content, attachments, recipientDepartmentIds, allDepartments, userDetails);
    }

    private ResponseEntity<?> createAnnouncementInternal(
            String titleRaw,
            String contentRaw,
            MultipartFile[] attachments,
            List<String> recipientDepartmentIds,
            boolean allDepartments,
            UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Vui long dang nhap de tao thong bao.");
        }

        Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Khong tim thay nguoi dung dang nhap.");
        }

        if (!"ROLE_DEPARTMENT".equalsIgnoreCase(currentUser.getRole())
                && !"ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(403).body("Ban khong co quyen tao thong bao.");
        }

        String title = titleRaw == null ? "" : titleRaw.trim();
        String content = contentRaw == null ? "" : contentRaw.trim();

        if (title.isEmpty() || content.isEmpty()) {
            return ResponseEntity.badRequest().body("Tieu de va noi dung khong duoc de trong.");
        }

        if (attachments != null) {
            long maxSizeBytes = 100L * 1024 * 1024;
            for (MultipartFile file : attachments) {
                if (file != null && !file.isEmpty() && file.getSize() > maxSizeBytes) {
                    return ResponseEntity.badRequest().body("Moi tep dinh kem phai <= 100MB.");
                }
            }
        }

        Announcement announcement = new Announcement();
        announcement.setId("ANN_" + UUID.randomUUID().toString().replace("-", ""));
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setDate(LocalDateTime.now());
        announcement.setUser(currentUser);

        Announcement saved = announcementRepository.save(announcement);

        try {
            fileAttachmentService.saveAnnouncementAttachments(saved, attachments);
            createAnnouncementNotification(saved, currentUser, recipientDepartmentIds, allDepartments);
        } catch (IOException e) {
            announcementRepository.deleteById(saved.getId());
            return ResponseEntity.status(500).body("Khong the luu tep dinh kem.");
        }

        Announcement finalSaved = announcementRepository.findById(saved.getId()).orElse(saved);
        return ResponseEntity.ok(announcementService.mapToDTO(finalSaved));
    }

    private void createAnnouncementNotification(Announcement announcement,
                                                Users creator,
                                                List<String> recipientDepartmentIds,
                                                boolean allDepartments) {
        List<Users> recipients = resolveStudentRecipients(creator, recipientDepartmentIds, allDepartments);
        if (recipients.isEmpty()) {
            return;
        }

        Notification notification = new Notification();
        notification.setId("NOTI_" + UUID.randomUUID().toString().replace("-", ""));
        notification.setTitle("Thông báo mới");
        notification.setContent(announcement.getTitle());
        notification.setNotificationType("SYSTEM_ANNOUNCEMENT_NOTIFICATION");
        notification.setRead(false);
        notification.setCreateAt(LocalDateTime.now());
        notification.setUsers(recipients);
        notificationRepo.save(notification);
    }

    private List<Users> resolveStudentRecipients(Users creator,
                                                 List<String> recipientDepartmentIds,
                                                 boolean allDepartments) {
        if (allDepartments) {
            return usersRepo.findByRole("ROLE_STUDENT");
        }

        Set<String> normalizedIds = new HashSet<>();
        if (recipientDepartmentIds != null) {
            normalizedIds.addAll(recipientDepartmentIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toSet()));
        }

        // Default to creator department when no department is selected.
        if (normalizedIds.isEmpty() && creator.getDepartment() != null && creator.getDepartment().getId() != null) {
            normalizedIds.add(creator.getDepartment().getId());
        }

        if (normalizedIds.isEmpty()) {
            return new ArrayList<>();
        }

        return usersRepo.findByRoleAndDepartment_IdIn("ROLE_STUDENT", normalizedIds);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        return updateAnnouncementInternal(
                id,
                payload.getOrDefault("title", ""),
                payload.getOrDefault("content", ""),
                null,
                null,
                userDetails
        );
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAnnouncementWithAttachments(
            @PathVariable String id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            @RequestParam(value = "removeAttachmentIds", required = false) List<String> removeAttachmentIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        return updateAnnouncementInternal(id, title, content, attachments, removeAttachmentIds, userDetails);
    }

    private ResponseEntity<?> updateAnnouncementInternal(
            String id,
            String titleRaw,
            String contentRaw,
            MultipartFile[] attachments,
            List<String> removeAttachmentIds,
            UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Vui long dang nhap de cap nhat thong bao.");
        }

        Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Khong tim thay nguoi dung dang nhap.");
        }

        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement == null) {
            return ResponseEntity.status(404).body("Thong bao khong ton tai.");
        }

        if (!canManageAnnouncement(currentUser, announcement)) {
            return ResponseEntity.status(403).body("Ban khong co quyen sua thong bao nay.");
        }

        String title = titleRaw == null ? "" : titleRaw.trim();
        String content = contentRaw == null ? "" : contentRaw.trim();

        if (title.isEmpty() || content.isEmpty()) {
            return ResponseEntity.badRequest().body("Tieu de va noi dung khong duoc de trong.");
        }

        if (attachments != null) {
            long maxSizeBytes = 100L * 1024 * 1024;
            for (MultipartFile file : attachments) {
                if (file != null && !file.isEmpty() && file.getSize() > maxSizeBytes) {
                    return ResponseEntity.badRequest().body("Moi tep dinh kem phai <= 100MB.");
                }
            }
        }

        announcement.setTitle(title);
        announcement.setContent(content);

        Announcement saved = announcementRepository.save(announcement);

        try {
            if (removeAttachmentIds != null && !removeAttachmentIds.isEmpty()) {
                List<String> normalizedIds = removeAttachmentIds.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .toList();

                fileAttachmentService.removeAnnouncementAttachments(saved.getId(), normalizedIds);
            }

            fileAttachmentService.saveAnnouncementAttachments(saved, attachments);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Khong the cap nhat tep dinh kem.");
        }

        Announcement refreshed = announcementRepository.findById(saved.getId()).orElse(saved);
        return ResponseEntity.ok(announcementService.mapToDTO(refreshed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Vui long dang nhap de xoa thong bao.");
        }

        Users currentUser = usersRepo.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Khong tim thay nguoi dung dang nhap.");
        }

        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement == null) {
            return ResponseEntity.status(404).body("Thong bao khong ton tai.");
        }

        if (!canManageAnnouncement(currentUser, announcement)) {
            return ResponseEntity.status(403).body("Ban khong co quyen xoa thong bao nay.");
        }

        announcementRepository.delete(announcement);
        return ResponseEntity.ok(Map.of("message", "Xoa thong bao thanh cong."));
    }

    private boolean canManageAnnouncement(Users currentUser, Announcement announcement) {
        if (currentUser == null || announcement == null) {
            return false;
        }

        if ("ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return true;
        }

        if (announcement.getUser() == null) {
            return false;
        }

        if (Objects.equals(currentUser.getId(), announcement.getUser().getId())) {
            return true;
        }

        return "ROLE_DEPARTMENT".equalsIgnoreCase(currentUser.getRole())
                && currentUser.getDepartment() != null
                && announcement.getUser().getDepartment() != null
                && Objects.equals(currentUser.getDepartment().getId(), announcement.getUser().getDepartment().getId());
    }
}