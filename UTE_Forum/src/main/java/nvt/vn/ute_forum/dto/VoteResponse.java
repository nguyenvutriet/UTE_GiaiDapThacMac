package nvt.vn.ute_forum.dto; // Bạn nên để trong package dto (Data Transfer Object)


import java.util.Map;

public class VoteResponse {

    private boolean isVoted;
    private String type;
    private long voteCount;
    private Map<String, Long> reactions;


    public VoteResponse() {
    }

    public VoteResponse(boolean isVoted, String type, long voteCount, Map<String, Long> reactions) {
        this.isVoted = isVoted;
        this.type = type;
        this.voteCount = voteCount;
        this.reactions = reactions;

    }

    // SỬA Ở ĐÂY: Getter chuẩn cho boolean
    public boolean isVoted() {
        return isVoted;
    }

    public void setVoted(boolean voted) {
        isVoted = voted;
    }

    // Các phần còn lại giữ nguyên
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getVoteCount() { return voteCount; }
    public void setVoteCount(long voteCount) { this.voteCount = voteCount; }


    // Ghi đè toString để bạn dễ Debug log khi cần
    @Override
    public String toString() {
        return "VoteResponse{" +
                "isVoted=" + isVoted +
                ", type='" + type + '\'' +
                ", voteCount=" + voteCount +
                '}';
    }
}