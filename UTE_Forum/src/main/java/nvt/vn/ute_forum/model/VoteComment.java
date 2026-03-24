package nvt.vn.ute_forum.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_comment")
public class VoteComment {

    @EmbeddedId
    private VoteCommentId id;

    // Lưu enum dưới dạng String trong database (LIKE, LOVE...) để dễ đọc
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    private LocalDateTime voteAt;

    public VoteComment() {
    }

    public VoteComment(VoteCommentId id, Users user, Comment comment, ReactionType type, LocalDateTime voteAt) {
        this.id = id;
        this.user = user;
        this.comment = comment;
        this.type = type;
        this.voteAt = voteAt;
    }

    public VoteComment(LocalDateTime voteAt) {
        this.voteAt = voteAt;
    }

    public LocalDateTime getVoteAt() {
        return voteAt;
    }

    public void setVoteAt(LocalDateTime voteAt) {
        this.voteAt = voteAt;
    }

    public VoteCommentId getId() {
        return id;
    }

    public void setId(VoteCommentId id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }

}
