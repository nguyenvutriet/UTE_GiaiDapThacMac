package nvt.vn.ute_forum.dto.dashboard;

public class MonthlyRadarDTO {
    private String month;      // T1, T2... T12
    private long resolvedCount;
    private long pendingCount;

    public MonthlyRadarDTO(String month, long resolvedCount, long pendingCount) {
        this.month = month;
        this.resolvedCount = resolvedCount;
        this.pendingCount = pendingCount;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
}
