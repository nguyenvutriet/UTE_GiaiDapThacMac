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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepo announcementRepository;
    @Autowired
    private AnnoucementService announcementService;

    @GetMapping
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Kiểm tra đăng nhập (Giống bên xóa comment của bạn)
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập thì xem bằng niềm tin hả má? 😂");
        }

        // 2. Thiết lập phân trang & Sắp xếp theo ngày mới nhất
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        // 3. Lấy dữ liệu từ Repo
        Page<Announcement> announcementPage = announcementRepository.findAll(pageable);

        // 4. Map từ Entity sang DTO bằng Service bạn đã viết
        // Việc này giúp Controller cực kỳ sạch sẽ, mọi logic map nằm ở Service
        Page<AnnouncementResponse> responsePage = announcementPage.map(announcementService::mapToDTO);

        // 5. Trả về kết quả
        return ResponseEntity.ok(responsePage);
    }


}