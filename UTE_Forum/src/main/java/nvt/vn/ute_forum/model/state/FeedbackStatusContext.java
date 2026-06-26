package nvt.vn.ute_forum.model.state;

import org.springframework.stereotype.Component;

@Component
public class FeedbackStatusContext {

    public FeedbackStatusState getState(String currentStatus) {
        if (currentStatus == null) {
            return new FinalState();
        }

        return switch (currentStatus) {
            case "PENDING" -> new PendingState();
            case "APPROVED" -> new ApprovedState();
            case "RESOLVED", "REJECTED" -> new FinalState();
            default -> new FinalState();
        };
    }

    public boolean canChange(String currentStatus, String newStatus) {
        return getState(currentStatus).canChangeTo(newStatus);
    }
}
