package nvt.vn.ute_forum.model.decorator;

import nvt.vn.ute_forum.dto.ForumPostDTO;

/**
 * Decorator Pattern — Concrete Decorator.
 *
 * Thêm badge "💬 Trending" vào subject của bài viết đạt ngưỡng bình luận.
 * Ngưỡng mặc định: commentCount >= 10.
 */
public class TrendingBadgeDecorator extends ForumPostDecorator {

    private static final long TRENDING_THRESHOLD = 10;
    private static final String TRENDING_PREFIX  = "💬 ";

    public TrendingBadgeDecorator(ForumPostDTO wrapped) {
        super(wrapped);
    }

    @Override
    public String getSubject() {
        if (wrapped.getCommentCount() >= TRENDING_THRESHOLD) {
            return TRENDING_PREFIX + wrapped.getSubject();
        }
        return wrapped.getSubject();
    }

    public String getBadge() {
        return wrapped.getCommentCount() >= TRENDING_THRESHOLD ? "TRENDING" : "";
    }
}