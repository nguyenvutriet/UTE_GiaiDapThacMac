package nvt.vn.ute_forum.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote")
public class Vote {

    @EmbeddedId
    private VoteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requestId")
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    private LocalDateTime voteAt;

    public Vote() {
    }

    public Vote(VoteId id, Users user, Request request, LocalDateTime voteAt) {
        this.id = id;
        this.user = user;
        this.request = request;
        this.voteAt = voteAt;
    }

    public Vote(LocalDateTime voteAt) {
        this.voteAt = voteAt;
    }

    public LocalDateTime getVoteAt() {
        return voteAt;
    }

    public void setVoteAt(LocalDateTime voteAt) {
        this.voteAt = voteAt;
    }

    public VoteId getId() {
        return id;
    }

    public void setId(VoteId id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }


}
