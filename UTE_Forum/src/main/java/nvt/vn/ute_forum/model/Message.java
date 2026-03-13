package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "createat", nullable = false)
    private LocalDate createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clarificationconversation_id")
    private ClarificationConversation clarificationConversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public Message() {
    }

    public Message(String id, String content, LocalDate createAt) {
        this.id = id;
        this.content = content;
        this.createAt = createAt;
    }

    public Message(String id, String content, LocalDate createAt, ClarificationConversation clarificationConversation, Users user) {
        this.id = id;
        this.content = content;
        this.createAt = createAt;
        this.clarificationConversation = clarificationConversation;
        this.user = user;
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

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public ClarificationConversation getClarificationConversation() {
        return clarificationConversation;
    }

    public void setClarificationConversation(ClarificationConversation clarificationConversation) {
        this.clarificationConversation = clarificationConversation;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
