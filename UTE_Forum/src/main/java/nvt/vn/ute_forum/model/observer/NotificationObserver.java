package nvt.vn.ute_forum.model.observer;

import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationObserver implements FeedbackObserver {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void update(Request request, Department fromDept, Department toDept, Users actor) {
        notificationService.createForwardNotifications(
                request,
                fromDept,
                toDept,
                actor
        );
    }
}
