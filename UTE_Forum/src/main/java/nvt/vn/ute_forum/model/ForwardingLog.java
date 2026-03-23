package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forwardinglog")
public class ForwardingLog {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "note")
    private String note;

    @Column(name = "forwardat", nullable = false)
    private LocalDateTime forwardAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fromdepartment_id", nullable = false)
    private Department fromdepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todepartment_id", nullable = false)
    private Department todepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public ForwardingLog() {
    }

    public ForwardingLog(String id, String message, String note, LocalDateTime forwardAt) {
        this.id = id;
        this.message = message;
        this.note = note;
        this.forwardAt = forwardAt;
    }

    public ForwardingLog(String id, String message, String note, LocalDateTime forwardAt, Department fromdepartment, Department todepartment, Request request, Users user) {
        this.id = id;
        this.message = message;
        this.note = note;
        this.forwardAt = forwardAt;
        this.fromdepartment = fromdepartment;
        this.todepartment = todepartment;
        this.request = request;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getForwardAt() {
        return forwardAt;
    }

    public void setForwardAt(LocalDateTime forwardAt) {
        this.forwardAt = forwardAt;
    }

    public Department getFromdepartment() {
        return fromdepartment;
    }

    public void setFromdepartment(Department fromdepartment) {
        this.fromdepartment = fromdepartment;
    }

    public Department getTodepartment() {
        return todepartment;
    }

    public void setTodepartment(Department todepartment) {
        this.todepartment = todepartment;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
