package nvt.vn.ute_forum.dto;

public class ReactionUserDTO {
    private String userId;
    private String userName;
    private String type;
    private String userRole;


    public ReactionUserDTO(String userId, String userName, String type, String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.userRole = userRole;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getType() { return type; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
}
