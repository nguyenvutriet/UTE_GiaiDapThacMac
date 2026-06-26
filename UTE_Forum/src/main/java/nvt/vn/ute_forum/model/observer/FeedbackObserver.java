package nvt.vn.ute_forum.model.observer;

import nvt.vn.ute_forum.model.*;

public interface FeedbackObserver {
    void update(Request request, Department fromDept, Department toDept, Users actor);
}
