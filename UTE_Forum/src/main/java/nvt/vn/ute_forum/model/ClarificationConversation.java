package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clarificationconversation")
public class ClarificationConversation {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "isopen", nullable = false)
    private Boolean isOpen;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "createat", nullable = false)
    private LocalDate createAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "clarificationConversation")
    @OrderBy("createAt ASC")
    private List<Message> messages = new ArrayList<>();

    public ClarificationConversation() {
    }

    public ClarificationConversation(String id, Boolean isOpen, LocalDate createAt) {
        this.id = id;
        this.isOpen = isOpen;
        this.createAt = createAt;
    }

    public ClarificationConversation(String id, Boolean isOpen, LocalDate createAt, Request request, List<Message> messages) {
        this.id = id;
        this.isOpen = isOpen;
        this.createAt = createAt;
        this.request = request;
        this.messages = messages;
    }

    public ClarificationConversation(String id, Boolean isOpen, String subject, LocalDate createAt, Request request, List<Message> messages) {
        this.id = id;
        this.isOpen = isOpen;
        this.subject = subject;
        this.createAt = createAt;
        this.request = request;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
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

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


}
