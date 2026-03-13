package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "request")
public class Request {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "subject")
    private String subject;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "currentstatus", nullable = false)
    private String currentStatus;

    @Column(name = "timecreate", nullable = false)
    private LocalDate timeCreate;

    @Column(name = "poststatus", nullable = false, columnDefinition = "VARCHAR(50) CHECK (poststatus IN ('PRIVATE', 'PUBLIC', 'HIDDEN'))")
    private String postStatus;          // PRIVATE, PUBLIC, PUBLIC->HIDDEN

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileAttachment> fileAttachments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "categorycontainrequest",
    joinColumns = {@JoinColumn(name = "request_id", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "category_id", referencedColumnName = "id")})
    private List<Category> categories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ClarificationConversation clarificationConversation;

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ForwardingLog> forwardingLogs = new ArrayList<>();

    public Request() {
    }

    public Request(String id, String subject, String description, String currentStatus, LocalDate timeCreate, String postStatus) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.currentStatus = currentStatus;
        this.timeCreate = timeCreate;
        this.postStatus = postStatus;
    }

    public Request(String id, String subject, String description, String currentStatus, LocalDate timeCreate, String postStatus, List<FileAttachment> fileAttachments, Department department, List<Category> categories, Users user, List<Comment> comments, ClarificationConversation clarificationConversation) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.currentStatus = currentStatus;
        this.timeCreate = timeCreate;
        this.postStatus = postStatus;
        this.fileAttachments = fileAttachments;
        this.department = department;
        this.categories = categories;
        this.user = user;
        this.comments = comments;
        this.clarificationConversation = clarificationConversation;
    }

    public Request(String id, String subject, String description, String currentStatus, LocalDate timeCreate, String postStatus, List<FileAttachment> fileAttachments, List<Category> categories, Department department, Users user, List<Comment> comments, ClarificationConversation clarificationConversation, List<ForwardingLog> forwardingLogs) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.currentStatus = currentStatus;
        this.timeCreate = timeCreate;
        this.postStatus = postStatus;
        this.fileAttachments = fileAttachments;
        this.categories = categories;
        this.department = department;
        this.user = user;
        this.comments = comments;
        this.clarificationConversation = clarificationConversation;
        this.forwardingLogs = forwardingLogs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDate getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(LocalDate timeCreate) {
        this.timeCreate = timeCreate;
    }

    public String getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
    }

    public List<FileAttachment> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<FileAttachment> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public ClarificationConversation getClarificationConversation() {
        return clarificationConversation;
    }

    public void setClarificationConversation(ClarificationConversation clarificationConversation) {
        this.clarificationConversation = clarificationConversation;
    }

    public List<ForwardingLog> getForwardingLogs() {
        return forwardingLogs;
    }

    public void setForwardingLogs(List<ForwardingLog> forwardingLogs) {
        this.forwardingLogs = forwardingLogs;
    }


}
