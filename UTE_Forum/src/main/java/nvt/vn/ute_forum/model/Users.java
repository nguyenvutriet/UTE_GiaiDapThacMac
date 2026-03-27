package nvt.vn.ute_forum.model;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name ="fullname", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false, unique = true)
    private String password;

    @Column(name = "role", nullable = false, columnDefinition = "VARCHAR(50) CHECK (role IN ('ROLE_ADMIN', 'ROLE_STUDENT', 'ROLE_DEPARTMENT'))")
    private String role;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "userreceivenotification", joinColumns = {@JoinColumn(name = "userid", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "notificationid", referencedColumnName = "id")})
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Announcement> announcements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Request> requests = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sender")
    private List<Message> messages = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "receiver")
    private List<Message> receivedMessages = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "student")
    private List<CommentReport> SubmitedcommentReports = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "admin")
    private List<CommentReport> ApprovedcommentReports = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<ForwardingLog> forwardingLogs = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Vote> votes = new ArrayList<>();


    public Users() {
    }

    public Users(String id, String fullName, String email, String password, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Users(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Users(String id, String fullName, String email, String password, String role, List<Notification> notifications, List<Announcement> announcements, Department department, List<Request> requests, List<Message> messages, List<Comment> comments, List<CommentReport> submitedcommentReports, List<CommentReport> approvedcommentReports) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.notifications = notifications;
        this.announcements = announcements;
        this.department = department;
        this.requests = requests;
        this.messages = messages;
        this.comments = comments;
        SubmitedcommentReports = submitedcommentReports;
        ApprovedcommentReports = approvedcommentReports;
    }

    public Users(String id, String fullName, String email, String password, String role, List<Notification> notifications, List<Announcement> announcements, Department department, List<Request> requests, List<Message> messages, List<Comment> comments, List<CommentReport> submitedcommentReports, List<CommentReport> approvedcommentReports, List<ForwardingLog> forwardingLogs) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.notifications = notifications;
        this.announcements = announcements;
        this.department = department;
        this.requests = requests;
        this.messages = messages;
        this.comments = comments;
        SubmitedcommentReports = submitedcommentReports;
        ApprovedcommentReports = approvedcommentReports;
        this.forwardingLogs = forwardingLogs;
    }

    public Users(String id, String fullName, String email, String password, String role, List<Announcement> announcements, List<Notification> notifications, Department department, List<Request> requests, List<Message> messages, List<Comment> comments, List<CommentReport> submitedcommentReports, List<CommentReport> approvedcommentReports, List<ForwardingLog> forwardingLogs, List<Vote> votes, List<Message> receivedMessages) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.announcements = announcements;
        this.notifications = notifications;
        this.department = department;
        this.requests = requests;
        this.messages = messages;
        this.comments = comments;
        SubmitedcommentReports = submitedcommentReports;
        ApprovedcommentReports = approvedcommentReports;
        this.forwardingLogs = forwardingLogs;
        this.votes = votes;
        this.receivedMessages = receivedMessages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<CommentReport> getSubmitedcommentReports() {
        return SubmitedcommentReports;
    }

    public void setSubmitedcommentReports(List<CommentReport> submitedcommentReports) {
        SubmitedcommentReports = submitedcommentReports;
    }

    public List<CommentReport> getApprovedcommentReports() {
        return ApprovedcommentReports;
    }

    public void setApprovedcommentReports(List<CommentReport> approvedcommentReports) {
        ApprovedcommentReports = approvedcommentReports;
    }

    public List<ForwardingLog> getForwardingLogs() {
        return forwardingLogs;
    }

    public void setForwardingLogs(List<ForwardingLog> forwardingLogs) {
        this.forwardingLogs = forwardingLogs;
    }


    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }


    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }
}