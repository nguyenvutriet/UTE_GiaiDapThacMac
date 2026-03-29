package nvt.vn.ute_forum.dto.dashboard;

public class CategoryStatDTO {
    private String categoryName;
    private long count;

    public CategoryStatDTO(String categoryName, long count) {
        this.categoryName = categoryName;
        this.count = count;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}