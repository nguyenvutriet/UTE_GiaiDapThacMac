package nvt.vn.ute_forum.model;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "createat", nullable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clarificationconversation_id")
    private ClarificationConversation clarificationConversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Users sender;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviever_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Users receiver;

    public Message() {
    }

    public Message(String id, String content, LocalDateTime createAt) {
        this.id = id;
        this.content = content;
        this.createAt = createAt;
    }

    public Message(String id,
                   String content,
                   LocalDateTime createAt,
                   ClarificationConversation clarificationConversation,
                   Users sender,
                   Users receiver) {
        this.id = id;
        this.content = content;
        this.createAt = createAt;
        this.clarificationConversation = clarificationConversation;
        this.sender = sender;
        this.receiver = receiver;
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

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public ClarificationConversation getClarificationConversation() {
        return clarificationConversation;
    }

    public void setClarificationConversation(ClarificationConversation clarificationConversation) {
        this.clarificationConversation = clarificationConversation;
    }

    public Users getSender() {
        return sender;
    }

    public void setSender(Users sender) {
        this.sender = sender;
    }


    public Users getReceiver() {
        return receiver;
    }

    public void setReceiver(Users receiver) {
        this.receiver = receiver;
    }


}