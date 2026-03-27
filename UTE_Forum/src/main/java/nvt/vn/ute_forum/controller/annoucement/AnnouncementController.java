package nvt.vn.ute_forum.controller.annoucement;


import nvt.vn.ute_forum.dto.AnnouncementResponse;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import nvt.vn.ute_forum.service.AnnoucementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepo announcementRepository;

    @Autowired
    private AnnoucementService announcementService;

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
}