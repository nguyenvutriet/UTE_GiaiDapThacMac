package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    public List<Notification> getByUserId(String userId) {
        return notificationRepo.findByUsers_IdOrderByCreateAtDesc(userId);
    }

    public long countUnreadByUserId(String userId) {
        return notificationRepo.countByUsers_IdAndIsReadFalse(userId);
    }

    @Transactional
    public boolean deleteForUser(String notificationId, String userId) {
        if (notificationId == null || notificationId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        if (!notificationRepo.existsByIdAndUsers_Id(notificationId, userId)) {
            return false;
        }

        int affectedRows = notificationRepo.unlinkNotificationFromUser(notificationId, userId);
        if (affectedRows <= 0) {
            return false;
        }

        if (notificationRepo.countReceivers(notificationId) == 0) {
            notificationRepo.deleteById(notificationId);
        }

        return true;
    }
}
