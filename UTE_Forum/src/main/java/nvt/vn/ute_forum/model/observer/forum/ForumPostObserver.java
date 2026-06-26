package nvt.vn.ute_forum.model.observer.forum;

import nvt.vn.ute_forum.dto.ForumPostDTO;

/**
 * Observer Pattern — Lắng nghe các sự kiện milestone của bài viết diễn đàn.
 *
 * Các event có thể xảy ra:
 *   - POST_HOT         : bài đạt ngưỡng reaction (10, 50, 100...)
 *   - POST_TRENDING    : bài đạt ngưỡng bình luận (10, 50...)
 *   - POST_BADGE_UPDATE: badge của bài thay đổi
 */
public interface ForumPostObserver {

    /**
     * @param event loại sự kiện (POST_HOT, POST_TRENDING...)
     * @param post  DTO bài viết kích hoạt sự kiện
     */
    void onPostEvent(ForumPostEvent event, ForumPostDTO post);
}