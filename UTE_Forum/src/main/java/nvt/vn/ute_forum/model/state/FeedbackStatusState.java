package nvt.vn.ute_forum.model.state;

public interface FeedbackStatusState {
    boolean canChangeTo(String newStatus);
}
