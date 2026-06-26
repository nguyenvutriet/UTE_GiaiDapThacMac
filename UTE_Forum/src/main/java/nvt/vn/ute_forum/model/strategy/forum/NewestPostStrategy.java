package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Sắp xếp bài viết theo thời gian tạo — mới nhất lên đầu.
 */
@Component
public class NewestPostStrategy implements ForumSortStrategy {

    @Override
    public String getName() {
        return "newest";
    }

    @Override
    public String getLabel() {
        return "Mới nhất";
    }

    @Override
    public List<ForumPostDTO> sort(List<ForumPostDTO> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(ForumPostDTO::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}