package nvt.vn.ute_forum.dto.dashboard;

import java.util.List;

public class DashboardDTO {
    // 4 Thẻ thống kê tổng quan (Ảnh 1)
    private long totalRequests;
    private long pendingRequests;
    private long resolvedRequests;
    private long rejectedRequests;

    // Biểu đồ: Xu hướng góp ý (Ảnh 1)
    private List<DailyTrendDTO> dailyTrends;

    // Biểu đồ: Danh mục hàng đầu (Ảnh 1)
    private List<CategoryStatDTO> topCategories;

    // Hiệu suất xử lý: Kim đồng hồ & Thời gian trung bình (Ảnh 2)
    private EfficiencyDTO efficiency;

    // Biểu đồ: Tổng quan năm - Radar Chart (Ảnh 2)
    private List<MonthlyRadarDTO> monthlyStats;

    public DashboardDTO() {}

    // Getters and Setters
    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }

    public long getResolvedRequests() { return resolvedRequests; }
    public void setResolvedRequests(long resolvedRequests) { this.resolvedRequests = resolvedRequests; }

    public long getRejectedRequests() { return rejectedRequests; }
    public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }

    public List<DailyTrendDTO> getDailyTrends() { return dailyTrends; }
    public void setDailyTrends(List<DailyTrendDTO> dailyTrends) { this.dailyTrends = dailyTrends; }

    public List<CategoryStatDTO> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryStatDTO> topCategories) { this.topCategories = topCategories; }

    public EfficiencyDTO getEfficiency() { return efficiency; }
    public void setEfficiency(EfficiencyDTO efficiency) { this.efficiency = efficiency; }

    public List<MonthlyRadarDTO> getMonthlyStats() { return monthlyStats; }
    public void setMonthlyStats(List<MonthlyRadarDTO> monthlyStats) { this.monthlyStats = monthlyStats; }
}
