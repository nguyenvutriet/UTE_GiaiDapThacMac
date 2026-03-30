package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Notification;
import nvt.vn.ute_forum.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.UsersRepo;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private UsersRepo usersRepo;

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

    // ================== HELPER ==================
    private Notification buildNoti(String content, String type, String title) {
        Notification n = new Notification();
        n.setId("NOTI_" + UUID.randomUUID().toString().substring(0, 6));
        n.setContent(content);
        n.setNotificationType(type);
        n.setTitle(title);
        n.setRead(false);
        n.setCreateAt(LocalDateTime.now());
        return n;
    }


    // ================== 1. FORWARD ==================
    public void createForwardNotifications(Request request,
                                           Department fromDept,
                                           Department toDept,
                                           Users staff) {

        String title = request.getSubject();

        // 🎓 Sinh viên
        Notification studentNoti = buildNoti(
                "Góp ý \"" + title + "\" đã được chuyển sang " + toDept.getName(),
                "FEEDBACK_FORWARDED_NOTIFICATION",
                "Góp ý được chuyển tiếp"
        );
        studentNoti.getUsers().add(request.getUser());
        notificationRepo.save(studentNoti);


        // 🏢 Phòng mới
        List<Users> toDeptUsers = usersRepo.findByDepartment(toDept);

        Notification deptNoti = buildNoti(
                "Bạn nhận được góp ý \"" + title + "\" từ " + fromDept.getName(),
                "FEEDBACK_FORWARDED_TO_YOU",
                "Góp ý mới"
        );
        deptNoti.setUsers(toDeptUsers);
        notificationRepo.save(deptNoti);


        // 🏢 Phòng hiện tại
        Notification staffNoti = buildNoti(
                "Bạn đã chuyển góp ý \"" + title + "\" sang " + toDept.getName(),
                "FEEDBACK_FORWARDED_BY_YOU",
                "Chuyển tiếp thành công"
        );
        staffNoti.getUsers().add(staff);
        notificationRepo.save(staffNoti);
    }


    // ================== 2. UPDATE STATUS ==================
    public void createStatusNotification(Request request, String status) {

        String title = request.getSubject();

        String content = "";
        String type = "";
        String notiTitle = "";

        switch (status) {
            case "APPROVED":
                content = "Góp ý \"" + title + "\" đang được xử lý";
                type = "FEEDBACK_PROCESSING_NOTIFICATION";
                notiTitle = "Đang xử lý góp ý";
                break;

            case "RESOLVED":
                content = "Góp ý \"" + title + "\" đã được xử lý hoàn tất";
                type = "FEEDBACK_RESOLVED_NOTIFICATION";
                notiTitle = "Góp ý đã hoàn tất";
                break;

            case "REJECTED":
                content = "Góp ý \"" + title + "\" đã bị từ chối";
                type = "FEEDBACK_REJECTED_NOTIFICATION";
                notiTitle = "Góp ý bị từ chối";
                break;
        }

        Notification noti = buildNoti(content, type, notiTitle);

        // 🎓 chỉ gửi cho sinh viên
        noti.getUsers().add(request.getUser());

        notificationRepo.save(noti);
    }

}
