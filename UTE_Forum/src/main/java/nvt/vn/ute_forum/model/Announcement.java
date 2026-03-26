package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "title")
    private String title;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @OneToMany(fetch =  FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "announcement")
    private List<FileAttachment> attachments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public Announcement() {
    }

    public Announcement(String id, String title,LocalDateTime date, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public Announcement(String id, String title,LocalDateTime date, String content, Users user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.date = date;
    }

    public Announcement(String id, String title, String content,LocalDateTime date, List<FileAttachment> attachments, Users user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.attachments = attachments;
        this.user = user;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileAttachment> attachments) {
        this.attachments = attachments;
    }
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}