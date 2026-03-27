package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.util.List;

@Repository
public interface RequestRepo extends JpaRepository<Request, String> {
    List<Request> findByUser_IdOrderByTimeCreateDesc(String userId);
    Optional<Request> findByIdAndUser_Id(String id, String userId);
    // Thêm Pageable vào đây và đổi kiểu trả về thành Page<Request>
    Page<Request> findByPostStatus(String status, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Request r " +
            "LEFT JOIN r.categories c " +
            "WHERE r.postStatus = 'PUBLIC' " +
            // Nếu :categoryId là null, điều kiện này luôn đúng (lấy tất cả)
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            // Nếu :departmentId là null, điều kiện này luôn đúng (lấy tất cả)
            "AND (:departmentId IS NULL OR r.department.id = :departmentId) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'newest' THEN r.timeCreate END DESC, " +
            "CASE WHEN :sortBy = 'oldest' THEN r.timeCreate END ASC")
    List<Request> findByFilters(
            @Param("categoryId") String categoryId,
            @Param("departmentId") String departmentId,
            @Param("sortBy") String sortBy
    );

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 5) AS UNSIGNED)), 0) FROM request WHERE id REGEXP '^REQ_[0-9]+$'", nativeQuery = true)
    Long findMaxRequestSequence();


    @Query("""
        SELECT r FROM Request r
        LEFT JOIN r.user u
        LEFT JOIN r.department d
        LEFT JOIN r.categories c
        WHERE LOWER(r.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY r.timeCreate DESC
        """)
    List<Request> searchByKeyword(@Param("keyword") String keyword);

    /*Chỉ lấy của phòng ban đang đăng nhập*/
    Page<Request> findByDepartment_Id(String departmentId, Pageable pageable);

    /*Tìm kiếm feedback theo key*/
    @Query("SELECT r FROM Request r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Request> findByContentContaining(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /*Cho department*/
    @Query("SELECT r FROM Request r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.department.id = :departmentId")
    Page<Request> findByContentContainingAndDepartment_Id(
            @Param("keyword") String keyword,
            @Param("departmentId") String departmentId,
            Pageable pageable
    );

}
