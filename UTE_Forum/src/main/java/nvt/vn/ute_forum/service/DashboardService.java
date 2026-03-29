package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.dashboard.*;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.RequestStatusHistory;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private RequestRepo requestRepo;

    public DashboardDTO getDashboardData(String deptId, LocalDateTime startDate, LocalDateTime endDate) {
        DashboardDTO dto = new DashboardDTO();

        // --- 1. Tổng quan con số (Ảnh 1) ---
        dto.setTotalRequests(requestRepo.countByDeptAndTime(deptId, startDate, endDate));
        dto.setPendingRequests(requestRepo.countByStatusDeptAndTime(deptId, "PENDING", startDate, endDate));
        dto.setResolvedRequests(requestRepo.countByStatusDeptAndTime(deptId, "RESOLVED", startDate, endDate));
        dto.setRejectedRequests(requestRepo.countByStatusDeptAndTime(deptId, "REJECTED", startDate, endDate));

        // --- 2. Xu hướng hàng ngày (DailyTrendDTO) ---
        List<DailyTrendDTO> dailyTrends = requestRepo.getDailyTrend(deptId, startDate, endDate)
                .stream()
                .map(obj -> new DailyTrendDTO(obj[0].toString(), (Long) obj[1]))
                .collect(Collectors.toList());
        dto.setDailyTrends(dailyTrends);

        // --- 3. Danh mục hàng đầu (CategoryStatDTO) ---
        List<CategoryStatDTO> categories = requestRepo.getTopCategories(deptId, startDate, endDate)
                .stream()
                .map(obj -> new CategoryStatDTO((String) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
        dto.setTopCategories(categories);

        // --- 4. Hiệu suất xử lý (EfficiencyDTO - Ảnh 2) ---
        dto.setEfficiency(calculateEfficiency(deptId, startDate, endDate));

        // --- 5. Thống kê Radar (MonthlyRadarDTO - Ảnh 2) ---
        // Logic: Lặp qua từng tháng trong khoảng startDate - endDate để đếm
        dto.setMonthlyStats(calculateMonthlyRadar(deptId, startDate, endDate));

        return dto;
    }

    private EfficiencyDTO calculateEfficiency(String deptId, LocalDateTime start, LocalDateTime end) {
        List<Request> resolvedList = requestRepo.findResolvedRequestsForEfficiency(deptId, start, end);
        double totalHours = 0;
        int count = 0;

        for (Request r : resolvedList) {
            // Tìm thời điểm chuyển sang RESOLVED trong history
            Optional<LocalDateTime> resolvedAt = r.getStatusHistory().stream()
                    .filter(h -> "RESOLVED".equals(h.getStatus()))
                    .map(RequestStatusHistory::getCreateAt)
                    .findFirst();

            if (resolvedAt.isPresent()) {
                long minutes = java.time.Duration.between(r.getTimeCreate(), resolvedAt.get()).toMinutes();
                totalHours += (double) minutes / 60;
                count++;
            }
        }

        EfficiencyDTO eff = new EfficiencyDTO();
        double avg = count == 0 ? 0 : totalHours / count;
        eff.setAvgHours(Math.round(avg * 10.0) / 10.0); // Làm tròn 1 chữ số (vd: 0.1h)
        eff.setPerformanceLevel(avg < 24 ? "Tốt" : (avg < 72 ? "Trung bình" : "Kém"));
        return eff;
    }

    private List<MonthlyRadarDTO> calculateMonthlyRadar(String deptId, LocalDateTime start, LocalDateTime end) {
        List<MonthlyRadarDTO> radarList = new ArrayList<>();
        // Logic đơn giản: Lặp từ tháng của start đến tháng của end
        LocalDateTime temp = start.withDayOfMonth(1).withHour(0).withMinute(0);
        while (temp.isBefore(end)) {
            LocalDateTime monthStart = temp;
            LocalDateTime monthEnd = temp.plusMonths(1).minusSeconds(1);

            long resolved = requestRepo.countByStatusDeptAndTime(deptId, "RESOLVED", monthStart, monthEnd);
            long pending = requestRepo.countByStatusDeptAndTime(deptId, "PENDING", monthStart, monthEnd);

            radarList.add(new MonthlyRadarDTO("T" + temp.getMonthValue(), resolved, pending));
            temp = temp.plusMonths(1);
        }
        return radarList;
    }
}
