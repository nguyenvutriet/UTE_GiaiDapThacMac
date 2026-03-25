package nvt.vn.ute_forum.dto;

public class ReactionUserDTO {
    private String userId;
    private String userName;
    private String type;

    public ReactionUserDTO(String userId, String userName, String type) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getType() { return type; }
}
