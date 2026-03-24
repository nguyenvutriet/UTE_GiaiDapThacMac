package nvt.vn.ute_forum.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

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



}
