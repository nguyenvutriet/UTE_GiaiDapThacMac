package nvt.vn.ute_forum.dto.dashboard;

public class EfficiencyDTO {
    private String departmentName;
    private double avgHours;        // Ví dụ: 0.1h
    private String performanceLevel; // Tốt, Trung bình, Kém

    public EfficiencyDTO() {}

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public double getAvgHours() { return avgHours; }
    public void setAvgHours(double avgHours) { this.avgHours = avgHours; }
    public String getPerformanceLevel() { return performanceLevel; }
    public void setPerformanceLevel(String performanceLevel) { this.performanceLevel = performanceLevel; }
}
