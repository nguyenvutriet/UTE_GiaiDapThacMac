package nvt.vn.ute_forum.dto;

import java.util.List;

public class AnnouncementResponse {
    private String id;
    private String title;
    private String content;
    private String date;
    private String creatorName; // Tên phòng ban lấy từ User -> Department
    private List<FileDTO> attachments; // Danh sách file chi tiết

    // --- Inner Class để chứa thông tin file ---
    public static class FileDTO {
        private String id;
        private String fileName;
        private String fileUrl;
        private String fileType;

        public FileDTO(String id, String fileName, String fileUrl, String fileType) {
            this.id = id;
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.fileType = fileType;
        }

        // Getters & Setters cho FileDTO
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
    }

    // --- Getters & Setters cho AnnouncementResponse ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public List<FileDTO> getAttachments() { return attachments; }
    public void setAttachments(List<FileDTO> attachments) { this.attachments = attachments; }
}
