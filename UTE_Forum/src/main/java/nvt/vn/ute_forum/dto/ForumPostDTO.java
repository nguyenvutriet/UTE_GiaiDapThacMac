package nvt.vn.ute_forum.dto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ForumPostDTO {
    private String id;
    private String subject;
    private String description;
    private String status;
    private LocalDateTime date;
    private String departmentName;
    private String userName;
    private List<String> categories;
    private long commentCount;
    private String reactionType;
    private String reactionTypeLower;
    private Map<String, Long> reactions;
    private long totalReactions;

    // 🔥 HÀM QUAN TRỌNG NHẤT ĐỂ HẾT LỖI 500
    // Thymeleaf sẽ gọi hàm này thông qua cú pháp: post.topReactionIcons
    public List<String> getTopReactionIcons() {
        if (this.reactions == null || this.reactions.isEmpty()) {
            return Collections.emptyList();
        }

        // Bản đồ chuyển đổi tên Reaction sang Emoji
        Map<String, String> emojiMap = Map.of(
                "LIKE", "👍",
                "LOVE", "❤️",
                "HAHA", "😆",
                "WOW", "😮",
                "SAD", "😢",
                "ANGRY", "😡"
        );

        return this.reactions.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Chỉ lấy những loại có người thả
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Nhiều nhất lên đầu
                .limit(3) // Lấy top 3 cái nhiều nhất
                .map(entry -> emojiMap.getOrDefault(entry.getKey().toUpperCase(), "👍"))
                .collect(Collectors.toList());
    }

    // --- Giữ nguyên các Getter/Setter cũ của bạn bên dưới ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
    public String getReactionTypeLower() { return reactionTypeLower; }
    public void setReactionTypeLower(String reactionTypeLower) { this.reactionTypeLower = reactionTypeLower; }
    public Map<String, Long> getReactions() { return reactions; }
    public void setReactions(Map<String, Long> reactions) { this.reactions = reactions; }
    public long getTotalReactions() {
        if(reactions == null) return 0;
        return reactions.values().stream().mapToLong(Long::longValue).sum();
    }
    public void setTotalReactions(long totalReactions) { this.totalReactions = totalReactions; }
}