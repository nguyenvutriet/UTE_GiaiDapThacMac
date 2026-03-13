package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "fileattachment")
public class FileAttachment {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "filename", nullable = false)
    private String fileName;

    @Column(name = "fileurl", nullable = false)
    private String fileUrl;

    @Column(name = "filetype", unique = true)
    private String fileType;

    @Column(name = "filesize", nullable = false)
    private int fileSize;

    @Column(name = "createat", nullable = false)
    private LocalDate createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    public FileAttachment() {
    }

    public FileAttachment(String id, String fileName, String fileUrl, String fileType, int fileSize, LocalDate createAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createAt = createAt;
    }

    public FileAttachment(String id, String fileName, String fileUrl, String fileType, int fileSize, LocalDate createAt, Request request) {
        this.id = id;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createAt = createAt;
        this.request = request;
    }

    public FileAttachment(String id, String fileName, String fileUrl, String fileType, int fileSize, LocalDate createAt, Announcement announcement) {
        this.id = id;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createAt = createAt;
        this.announcement = announcement;
    }

    public FileAttachment(String fileName, String id, String fileUrl, String fileType, int fileSize, LocalDate createAt, Request request, Announcement announcement) {
        this.fileName = fileName;
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createAt = createAt;
        this.request = request;
        this.announcement = announcement;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }


}
