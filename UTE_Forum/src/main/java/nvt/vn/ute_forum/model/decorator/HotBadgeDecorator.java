package nvt.vn.ute_forum.model.decorator;

import nvt.vn.ute_forum.dto.ForumPostDTO;

/**
 * Decorator Pattern — Concrete Decorator.
 *
 * Thêm badge "🔥 Hot" vào subject của bài viết đạt ngưỡng reaction.
 * Ngưỡng mặc định: totalReactions >= 10.
 */
public class HotBadgeDecorator extends ForumPostDecorator {

    private static final long HOT_THRESHOLD = 10;
    private static final String HOT_PREFIX  = "🔥 ";

    public HotBadgeDecorator(ForumPostDTO wrapped) {
        super(wrapped);
    }

    /** Trả về subject có thêm prefix nếu đủ điều kiện hot */
    @Override
    public String getSubject() {
        if (wrapped.getTotalReactions() >= HOT_THRESHOLD) {
            return HOT_PREFIX + wrapped.getSubject();
        }
        return wrapped.getSubject();
    }

    /**
     * Badge name để Thymeleaf / JS kiểm tra và render nhãn riêng.
     * Thêm field getBadge() vào DTO nếu muốn render nhãn tách biệt.
     */
    public String getBadge() {
        return wrapped.getTotalReactions() >= HOT_THRESHOLD ? "HOT" : "";
    }
}