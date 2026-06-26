package nvt.vn.ute_forum.model.state;

public class ApprovedState implements FeedbackStatusState {

    @Override
    public boolean canChangeTo(String newStatus) {
        return newStatus.equals("RESOLVED")
                || newStatus.equals("REJECTED")
                || newStatus.equals("FORWARDING");
    }
}
