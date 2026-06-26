package nvt.vn.ute_forum.model.observer.forum;

/**
 * Các loại sự kiện milestone của bài viết diễn đàn.
 */
public enum ForumPostEvent {

    /** Bài đạt mốc reaction (10, 50, 100) */
    POST_HOT,

    /** Bài đạt mốc bình luận (10, 50) */
    POST_TRENDING,

    /** Badge bài viết thay đổi */
    POST_BADGE_UPDATE
}