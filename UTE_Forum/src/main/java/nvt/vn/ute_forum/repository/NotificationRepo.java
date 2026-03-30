package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, String> {
	List<Notification> findByUsers_IdOrderByCreateAtDesc(String userId);
	long countByUsers_IdAndIsReadFalse(String userId);
	Optional<Notification> findByIdAndUsers_Id(String notificationId, String userId);
	Optional<Notification> findByNotificationType(String notificationType);
}
