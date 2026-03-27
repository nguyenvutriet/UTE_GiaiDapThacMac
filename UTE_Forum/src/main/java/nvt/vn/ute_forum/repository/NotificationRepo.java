package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, String> {
	List<Notification> findByUsers_IdOrderByCreateAtDesc(String userId);

	long countByUsers_IdAndIsReadFalse(String userId);

	boolean existsByIdAndUsers_Id(String notificationId, String userId);

	@Modifying
	@Query(value = "DELETE FROM userreceivenotification WHERE notificationid = :notificationId AND userid = :userId", nativeQuery = true)
	int unlinkNotificationFromUser(@Param("notificationId") String notificationId, @Param("userId") String userId);

	@Query(value = "SELECT COUNT(*) FROM userreceivenotification WHERE notificationid = :notificationId", nativeQuery = true)
	long countReceivers(@Param("notificationId") String notificationId);

	@Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 6) AS UNSIGNED)), 0) FROM notification WHERE id REGEXP '^NOTI_[0-9]+$'", nativeQuery = true)
	Long findMaxNotificationSequence();
}