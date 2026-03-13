package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "notificationtype", nullable = false, unique = true, columnDefinition = "VARCHAR(255) CHECK (notificationtype IN ('VOTE_FORUM_POST_NOTIFICATION', 'COMMENT_FORUM_POST_NOTIFICATION', 'REPLY_COMMENT_FORUM_POST_NOTIFICATION', 'NEW_ANNOUNCEMENT_NOTIFICATION'" +
            ", 'VOTE_ANNOUNCEMENT_NOTIFICATION', 'COMMENT_ANNOUNCEMENT_NOTIFICATION', 'REPLY_COMMENT_ANNOUNCEMENT_NOTIFICATION', 'REPORT_SUBMITTED_CONFIRMATION', 'REPORT_RESOLVED_VIOLATION', 'REPORT_RESOLVED_NO_VIOLATION', 'YOUR_COMMENT_WAS_DELETED', 'MESSAGE_NEW_NOTIFICATION'," +
            ", 'FEEDBACK_SUBMITTED_NOTIFICATION', 'FEEDBACK_PROCESSING_NOTIFICATION', 'FEEDBACK_RESOLVED_NOTIFICATION', 'FEEDBACK_REJECTED_NOTIFICATION', 'NEW_FEEDBACK_RECEIVED', 'FEEDBACK_FORWARDED_TO_YOU', 'NEW_COMMENT_REPORT_FOR_ADMIN', 'ADMIN_NOTIFICATION', 'SYSTEM_ANNOUNCEMENT_NOTIFICATION'))")
    private String notificationType;

    @Column(name = "title")
    private String title;

    @Column(name = "isread", nullable = false)
    private Boolean isRead;

    @Column(name = "createat", nullable = false)
    private LocalDate createAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "userreceivenotofication", joinColumns = {@JoinColumn(name = "notificationid", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "userid", referencedColumnName = "id")})
    private List<Users> users = new ArrayList<>();

    public Notification() {
    }

    public Notification(String id, String content, String notificationType, String title, Boolean isRead, LocalDate createAt) {
        this.id = id;
        this.content = content;
        this.notificationType = notificationType;
        this.title = title;
        this.isRead = isRead;
        this.createAt = createAt;
    }

    public Notification(String id, String content, String notificationType, String title, Boolean isRead, LocalDate createAt, List<Users> users) {
        this.id = id;
        this.content = content;
        this.notificationType = notificationType;
        this.title = title;
        this.isRead = isRead;
        this.createAt = createAt;
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }


}
