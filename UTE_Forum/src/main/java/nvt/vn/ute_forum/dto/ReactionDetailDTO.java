package nvt.vn.ute_forum.dto;

import nvt.vn.ute_forum.model.ReactionType;

public class ReactionDetailDTO {
    private String userId;
    private String fullName;
    private ReactionType type;

    public ReactionDetailDTO(String userId, String fullName, ReactionType type) {
        this.userId = userId;
        this.fullName = fullName;
        this.type = type;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public ReactionType getType() { return type; }
}