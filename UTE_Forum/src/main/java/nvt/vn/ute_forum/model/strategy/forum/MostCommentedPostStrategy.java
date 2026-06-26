package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Sắp xếp bài viết theo số lượng bình luận — nhiều nhất lên đầu.
 */
@Component
public class MostCommentedPostStrategy implements ForumSortStrategy {

    @Override
    public String getName() {
        return "most_commented";
    }

    @Override
    public String getLabel() {
        return "Nhiều bình luận nhất";
    }

    @Override
    public List<ForumPostDTO> sort(List<ForumPostDTO> posts) {
        return posts.stream()
                .sorted(Comparator.comparingLong(ForumPostDTO::getCommentCount).reversed())
                .toList();
    }
}