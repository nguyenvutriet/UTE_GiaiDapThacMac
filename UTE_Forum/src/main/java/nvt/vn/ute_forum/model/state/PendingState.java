package nvt.vn.ute_forum.model.state;

public class PendingState implements FeedbackStatusState {

    @Override
    public boolean canChangeTo(String newStatus) {
        return newStatus.equals("APPROVED")
                || newStatus.equals("RESOLVED")
                || newStatus.equals("REJECTED");
    }
}
