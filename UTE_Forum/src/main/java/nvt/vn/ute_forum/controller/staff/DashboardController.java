package nvt.vn.ute_forum.controller.staff;

import nvt.vn.ute_forum.dto.dashboard.DashboardDTO;
import nvt.vn.ute_forum.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/staff/dashboard")
@CrossOrigin(origins = "*") // Hỗ trợ gọi API từ Frontend khác port
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

//    /**
//     * API lấy dữ liệu thống kê cho Dashboard
//     * @param deptId: ID của phòng ban
//     * @param startStr: Ngày bắt đầu (định dạng dd/MM/yyyy)
//     * @param endStr: Ngày kết thúc (định dạng dd/MM/yyyy)
//     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardDTO> getStats(
            @RequestParam String deptId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // 1. Định nghĩa formatter để khớp với định dạng dd/MM/yyyy từ ảnh
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 2. Chuyển đổi String sang LocalDateTime
        // Start date: 00:00:00 của ngày đó
        LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();

        // End date: 23:59:59 của ngày đó
        LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX);

        // 3. Gọi service để lấy dữ liệu đã được lọc theo thời gian
        DashboardDTO result = dashboardService.getDashboardData(deptId, start, end);

        return ResponseEntity.ok(result);
    }
}
