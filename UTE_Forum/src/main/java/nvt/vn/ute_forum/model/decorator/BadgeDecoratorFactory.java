package nvt.vn.ute_forum.model.decorator;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decorator Pattern — Factory hỗ trợ.
 *
 * Tự động áp dụng các decorator phù hợp cho từng bài viết
 * dựa trên ngưỡng reaction và comment.
 *
 * Thứ tự ưu tiên: Hot > Trending > không badge.
 * Khi cả hai đều thỏa → chỉ áp Hot (tránh prefix kép).
 */
@Component
public class BadgeDecoratorFactory {

    /**
     * Áp dụng decorator phù hợp cho một bài viết.
     *
     * @param post bài viết gốc
     * @return bài viết đã được trang trí badge (hoặc nguyên bản nếu chưa đủ ngưỡng)
     */
    public ForumPostDTO decorate(ForumPostDTO post) {
        if (post == null) return null;

        // Hot có ưu tiên cao hơn Trending
        if (post.getTotalReactions() >= 10) {
            return new HotBadgeDecorator(post);
        }

        if (post.getCommentCount() >= 10) {
            return new TrendingBadgeDecorator(post);
        }

        return post; // không đủ ngưỡng → trả nguyên
    }

    /**
     * Áp dụng decorator cho toàn bộ danh sách.
     */
    public List<ForumPostDTO> decorateAll(List<ForumPostDTO> posts) {
        if (posts == null) return List.of();
        return posts.stream()
                .map(this::decorate)
                .toList();
    }
}