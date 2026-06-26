package nvt.vn.ute_forum.model.observer.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observer Pattern — Publisher (Subject).
 *
 * Kiểm tra milestone sau mỗi lần reaction/comment thay đổi,
 * nếu đạt ngưỡng thì notify tất cả observer đã đăng ký.
 *
 * Ngưỡng hiện tại:
 *   - HOT      : totalReactions >= 10 | 50 | 100
 *   - TRENDING : commentCount   >= 10 | 50
 */
@Component
public class ForumPostEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ForumPostEventPublisher.class);

    private static final long[] REACTION_MILESTONES = {10, 50, 100};
    private static final long[] COMMENT_MILESTONES  = {10, 50};

    private final List<ForumPostObserver> observers;

    @Autowired
    public ForumPostEventPublisher(List<ForumPostObserver> observers) {
        this.observers = observers;
    }

    /**
     * Gọi sau khi reaction bài viết thay đổi.
     * Nếu totalReactions vừa đạt milestone → notify POST_HOT.
     */
    public void checkReactionMilestone(ForumPostDTO post) {
        long total = post.getTotalReactions();
        for (long milestone : REACTION_MILESTONES) {
            if (total == milestone) {
                log.info("[Forum] Bài '{}' đạt {} reactions → POST_HOT", post.getId(), milestone);
                notify(ForumPostEvent.POST_HOT, post);
                return;
            }
        }
    }

    /**
     * Gọi sau khi số bình luận bài viết thay đổi.
     * Nếu commentCount vừa đạt milestone → notify POST_TRENDING.
     */
    public void checkCommentMilestone(ForumPostDTO post) {
        long count = post.getCommentCount();
        for (long milestone : COMMENT_MILESTONES) {
            if (count == milestone) {
                log.info("[Forum] Bài '{}' đạt {} bình luận → POST_TRENDING", post.getId(), milestone);
                notify(ForumPostEvent.POST_TRENDING, post);
                return;
            }
        }
    }

    private void notify(ForumPostEvent event, ForumPostDTO post) {
        for (ForumPostObserver observer : observers) {
            try {
                observer.onPostEvent(event, post);
            } catch (Exception e) {
                log.error("[Forum] Observer {} lỗi khi xử lý {}: {}",
                        observer.getClass().getSimpleName(), event, e.getMessage());
            }
        }
    }
}