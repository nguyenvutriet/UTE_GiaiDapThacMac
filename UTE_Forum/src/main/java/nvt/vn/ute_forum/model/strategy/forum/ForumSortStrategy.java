package nvt.vn.ute_forum.model.strategy.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import java.util.List;

/**
 * Strategy Pattern — Chiến lược sắp xếp bài viết diễn đàn.
 *
 * Mỗi implementation định nghĩa một cách sắp xếp khác nhau.
 * ForumSortContext sẽ nhận strategy phù hợp và gọi sort().
 */
public interface ForumSortStrategy {

    /**
     * Tên định danh để map từ request param (vd: "most_liked", "most_commented").
     */
    String getName();

    /**
     * Nhãn hiển thị trên UI.
     */
    String getLabel();

    /**
     * Thực hiện sắp xếp danh sách bài viết.
     *
     * @param posts danh sách ForumPostDTO chưa sắp xếp
     * @return danh sách đã sắp xếp
     */
    List<ForumPostDTO> sort(List<ForumPostDTO> posts);
}