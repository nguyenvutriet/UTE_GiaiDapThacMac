package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "requeststatushistory")
public class RequestStatusHistory {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(100) CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'RESOLVED', 'FORWARDING'))")
    private String status;

    @Column(name = "createat", nullable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    public RequestStatusHistory() {
    }

    public RequestStatusHistory(String id, String status, LocalDateTime createAt) {
        this.id = id;
        this.status = status;
        this.createAt = createAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }
}
