package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, String> {

    // =====================================================================
    // COMMENT GỐC (top-level) — parentId IS NULL
    // =====================================================================

    /** Lấy tất cả comment gốc (kể cả bị ẩn) — dùng cho admin */
    @Query("SELECT c FROM Comment c WHERE c.request.id = :requestId AND c.parentId IS NULL ORDER BY c.date ASC")
    List<Comment> findRootByRequestId(@Param("requestId") String requestId);

    /** Lấy comment gốc đang active — dùng cho user thường */
    @Query("SELECT c FROM Comment c WHERE c.request.id = :requestId AND c.parentId IS NULL AND (c.isActive = true OR c.isActive IS NULL) ORDER BY c.date ASC")
    List<Comment> findActiveRootByRequestId(@Param("requestId") String requestId);

    // =====================================================================
    // REPLY — parentId IS NOT NULL
    // =====================================================================

    /**
     * Lấy TẤT CẢ reply trong 1 thread (theo parentId = ID comment gốc).
     * Service dùng query này để build cây Composite 1 lần duy nhất,
     * tránh N+1 query (không dùng lazy load từng comment một).
     */
    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId AND (c.isActive = true OR c.isActive IS NULL) ORDER BY c.date ASC")
    List<Comment> findActiveRepliesByParentId(@Param("parentId") String parentId);

    /** Lấy tất cả reply kể cả bị ẩn — cho admin */
    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId ORDER BY c.date ASC")
    List<Comment> findAllRepliesByParentId(@Param("parentId") String parentId);

    // =====================================================================
    // GIỮ LẠI — tương thích ngược với code cũ
    // =====================================================================

    List<Comment> findByRequestId(String requestId);

    long countByRequest_Id(String requestId);

    @Query("SELECT c FROM Comment c WHERE c.request.id = :requestId AND (c.isActive = true OR c.isActive IS NULL)")
    List<Comment> findActiveByRequestId(@Param("requestId") String requestId);

    @Query("""
            SELECT c.id, c.request.id, c.user.id, c.user.fullName, c.date
            FROM Comment c
            WHERE c.request.user.id = :ownerUserId
              AND c.user.id <> :ownerUserId
              AND (c.isActive = true OR c.isActive IS NULL)
            ORDER BY c.date DESC
            """)
    List<Object[]> findCommentNotificationDataByOwnerId(@Param("ownerUserId") String ownerUserId);
}