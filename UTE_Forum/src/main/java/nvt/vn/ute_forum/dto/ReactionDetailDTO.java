package nvt.vn.ute_forum.dto;
import nvt.vn.ute_forum.model.ReactionType;

public class ReactionDetailDTO {
    private String userId;
    private String fullName;
    private ReactionType type;
    private String userRole; // 🔥 THÊM CÁI NÀY

    // Cập nhật Constructor để nhận thêm role
    public ReactionDetailDTO(String userId, String fullName, ReactionType type, String userRole) {
        this.userId = userId;
        this.fullName = fullName;
        this.type = type;
        this.userRole = userRole;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public ReactionType getType() { return type; }
    public String getUserRole() { return userRole; } // 🔥 THÊM GETTER
}