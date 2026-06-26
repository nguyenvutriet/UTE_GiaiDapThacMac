package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Sắp xếp bài viết theo điểm "Hot Score" — kết hợp nhiều yếu tố.
 *
 * Công thức tính điểm:
 *   hotScore = (reactions * 2) + (comments * 3) + recencyBonus
 *
 * recencyBonus: bài viết trong 24h được cộng thêm 50 điểm,
 *               trong 72h cộng thêm 20 điểm.
 *
 * Mục tiêu: ưu tiên bài vừa có nhiều tương tác VÀ còn mới.
 */
@Component
public class HotTrendingPostStrategy implements ForumSortStrategy {

    private static final double REACTION_WEIGHT  = 2.0;
    private static final double COMMENT_WEIGHT   = 3.0;
    private static final double BONUS_24H        = 50.0;
    private static final double BONUS_72H        = 20.0;

    @Override
    public String getName() {
        return "hot_trending";
    }

    @Override
    public String getLabel() {
        return "🔥 Đang hot";
    }

    @Override
    public List<ForumPostDTO> sort(List<ForumPostDTO> posts) {
        LocalDateTime now = LocalDateTime.now();

        return posts.stream()
                .sorted(Comparator.comparingDouble(
                        (ForumPostDTO p) -> calculateHotScore(p, now)
                ).reversed())
                .toList();
    }

    private double calculateHotScore(ForumPostDTO post, LocalDateTime now) {
        double score = (post.getTotalReactions() * REACTION_WEIGHT)
                + (post.getCommentCount()   * COMMENT_WEIGHT);

        if (post.getDate() != null) {
            long hoursAgo = Duration.between(post.getDate(), now).toHours();
            if (hoursAgo <= 24) {
                score += BONUS_24H;
            } else if (hoursAgo <= 72) {
                score += BONUS_72H;
            }
        }

        return score;
    }
}