package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "commentreport")
public class CommentReport {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status = "pending";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Users admin;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public CommentReport() {
    }

    public CommentReport(String id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public CommentReport(String id, String reason, Comment comment, Users student, Users admin) {
        this.id = id;
        this.reason = reason;
        this.comment = comment;
        this.status = "pending";
        this.student = student;
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Users getStudent() {
        return student;
    }

    public void setStudent(Users student) {
        this.student = student;
    }

    public Users getAdmin() {
        return admin;
    }
    public void setAdmin(Users admin) {
        this.admin = admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
