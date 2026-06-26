package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Sắp xếp bài viết theo tổng số lượt reaction — nhiều nhất lên đầu.
 *
 * totalReactions = LIKE + LOVE + HAHA + WOW + SAD + ANGRY
 */
@Component
public class MostLikedPostStrategy implements ForumSortStrategy {

    @Override
    public String getName() {
        return "most_liked";
    }

    @Override
    public String getLabel() {
        return "Nhiều lượt thích nhất";
    }

    @Override
    public List<ForumPostDTO> sort(List<ForumPostDTO> posts) {
        return posts.stream()
                .sorted(Comparator.comparingLong(ForumPostDTO::getTotalReactions).reversed())
                .toList();
    }
}