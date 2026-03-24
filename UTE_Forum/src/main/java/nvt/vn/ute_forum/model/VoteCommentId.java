package nvt.vn.ute_forum.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VoteCommentId implements Serializable {

    private String userId;

    private String commentId;

    public VoteCommentId() {
    }

    public VoteCommentId(String userId, String commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoteCommentId that = (VoteCommentId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(commentId, that.commentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, commentId);
    }
}
