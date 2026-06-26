package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy Pattern — Context.
 *
 * Nhận danh sách tất cả ForumSortStrategy được Spring inject,
 * map chúng theo getName() để tra cứu nhanh O(1).
 *
 * Sử dụng:
 *   List<ForumPostDTO> sorted = forumSortContext.sort("most_liked", posts);
 */
@Component
public class ForumSortContext {

    /** Map: strategyName → strategy instance */
    private final Map<String, ForumSortStrategy> strategyMap;

    /** Chiến lược mặc định khi name không khớp */
    private static final String DEFAULT_STRATEGY = "newest";

    @Autowired
    public ForumSortContext(List<ForumSortStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ForumSortStrategy::getName,
                        Function.identity()
                ));
    }

    /**
     * Sắp xếp danh sách bài viết theo chiến lược chỉ định.
     *
     * @param strategyName tên strategy (vd: "most_liked", "newest")
     * @param posts        danh sách bài viết
     * @return danh sách đã sắp xếp
     */
    public List<ForumPostDTO> sort(String strategyName, List<ForumPostDTO> posts) {
        if (posts == null || posts.isEmpty()) return List.of();

        ForumSortStrategy strategy = resolve(strategyName);
        return strategy.sort(posts);
    }

    /**
     * Trả về danh sách tất cả strategy để render option trên UI.
     */
    public List<SortOption> getAvailableOptions() {
        return strategyMap.values().stream()
                .map(s -> new SortOption(s.getName(), s.getLabel()))
                .sorted(java.util.Comparator.comparing(SortOption::name))
                .toList();
    }

    private ForumSortStrategy resolve(String name) {
        if (name == null || name.isBlank()) {
            return strategyMap.get(DEFAULT_STRATEGY);
        }
        return strategyMap.getOrDefault(name.trim().toLowerCase(),
                strategyMap.get(DEFAULT_STRATEGY));
    }

    /** DTO nhỏ để truyền options ra View */
    public record SortOption(String name, String label) {}
}