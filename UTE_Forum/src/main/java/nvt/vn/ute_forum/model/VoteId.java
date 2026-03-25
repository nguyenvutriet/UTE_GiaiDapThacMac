package nvt.vn.ute_forum.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VoteId implements Serializable {

    private String userId;

    private String requestId;

    public VoteId() {
    }

    public VoteId(String userId, String requestId) {
        this.userId = userId;
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoteId that = (VoteId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, requestId);
    }


}
