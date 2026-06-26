package nvt.vn.ute_forum.model.state;

public class FinalState implements FeedbackStatusState {

    @Override
    public boolean canChangeTo(String newStatus) {
        return false;
    }
}