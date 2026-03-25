package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepo extends JpaRepository<Request, String> {
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




}
