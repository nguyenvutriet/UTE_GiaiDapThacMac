package nvt.vn.ute_forum.dto.dashboard;

public class DailyTrendDTO {
    private String date; // Định dạng "dd-MM"
    private long count;

    public DailyTrendDTO(String date, long count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
