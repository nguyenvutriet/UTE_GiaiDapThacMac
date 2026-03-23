package nvt.vn.ute_forum.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private String userName;
    private String content;
    private LocalDateTime date;
    private String id;

    // 1. Bạn phải có Constructor KHÔNG tham số (để tránh lỗi Jackson)
    public CommentDTO() {
    }

    // 2. Sửa Constructor này ĐÚNG THỨ TỰ bạn gọi ở Controller
    public CommentDTO(String userName, String content, LocalDateTime date, String id) {
        this.userName = userName;
        this.content = content;
        this.date = date;
        this.id = id;
    }

    // Getter và Setter (Bắt buộc phải có để hiển thị lên giao diện)
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

}