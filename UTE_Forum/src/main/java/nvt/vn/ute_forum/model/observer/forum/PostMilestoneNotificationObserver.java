package nvt.vn.ute_forum.model.observer.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observer Pattern — Concrete Observer.
 *
 * Khi bài viết đạt milestone reaction hoặc bình luận,
 * gửi thông báo cho tác giả của bài viết đó.
 */
@Component
public class PostMilestoneNotificationObserver implements ForumPostObserver {

    private static final Logger log = LoggerFactory.getLogger(PostMilestoneNotificationObserver.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RequestRepo requestRepo;

    @Override
    public void onPostEvent(ForumPostEvent event, ForumPostDTO post) {
        // Lấy thông tin bài viết gốc để lấy Users entity
        requestRepo.findById(post.getId()).ifPresent(request -> {
            Users author = request.getUser();
            if (author == null) return;

            String title, message;

            switch (event) {
                case POST_HOT -> {
                    title   = "🔥 Bài viết của bạn đang hot!";
                    message = String.format(
                            "Bài \"%s\" vừa đạt %d lượt bày tỏ cảm xúc!",
                            post.getSubject(), post.getTotalReactions()
                    );
                }
                case POST_TRENDING -> {
                    title   = "💬 Bài viết của bạn đang trending!";
                    message = String.format(
                            "Bài \"%s\" vừa đạt %d bình luận!",
                            post.getSubject(), post.getCommentCount()
                    );
                }
                default -> {
                    return; // Không xử lý event khác
                }
            }

            notificationService.createNotificationForUsers(
                    event.name(),
                    title,
                    message,
                    List.of(author),
                    post.getId()
            );

            log.info("[Forum] Đã gửi thông báo '{}' cho tác giả {}", event, author.getId());
        });
    }
}