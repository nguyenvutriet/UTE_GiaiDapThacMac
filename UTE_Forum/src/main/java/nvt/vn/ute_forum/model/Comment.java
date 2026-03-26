package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "isactive")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentComment")
    private List<Comment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "comment")
    private List<CommentReport> commentReports = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "comment")
    private List<VoteComment> voteComments = new ArrayList<>();

    public Comment() {
    }

    public Comment(String id, String content, LocalDateTime date) {
        this.id = id;
        this.content = content;
        this.date = date;
    }

    public Comment(String id, String content, LocalDateTime date, Request request, Users user, List<CommentReport> commentReports) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.request = request;
        this.user = user;
        this.commentReports = commentReports;
        this.voteComments = voteComments;
    }

    public Comment(String id, String content, LocalDateTime date, Request request, Users user, List<Comment> replies, Comment parentComment, List<CommentReport> commentReports, List<VoteComment> voteComments) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.request = request;
        this.user = user;
        this.replies = replies;
        this.parentComment = parentComment;
        this.commentReports = commentReports;
        this.voteComments = voteComments;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public List<CommentReport> getCommentReports() {
        return commentReports;
    }

    public void setCommentReports(List<CommentReport> commentReports) {
        this.commentReports = commentReports;
    }
    // Thêm Getter và Setter cho voteComments
    public List<VoteComment> getVoteComments() {
        return voteComments;
    }

    public void setVoteComments(List<VoteComment> voteComments) {
        this.voteComments = voteComments;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
