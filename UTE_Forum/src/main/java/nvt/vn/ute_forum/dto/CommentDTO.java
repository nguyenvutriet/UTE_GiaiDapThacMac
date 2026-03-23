package nvt.vn.ute_forum.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class CommentDTO {
    private String userName;
    private String content;
    private LocalDateTime date;
    private String id;
    private boolean canDelete;

    // 🔥 THÊM 2 CÁI NÀY
    private String reactionType;
    private Map<String, Long> reactions;

    public CommentDTO() {}

    public CommentDTO(String userName, String content, LocalDateTime date, String id, boolean canDelete) {
        this.userName = userName;
        this.content = content;
        this.date = date;
        this.id = id;
        this.canDelete = canDelete;
    }

    // Getter Setter
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    // 🔥 Getter Setter mới
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public Map<String, Long> getReactions() { return reactions; }
    public void setReactions(Map<String, Long> reactions) { this.reactions = reactions; }
}