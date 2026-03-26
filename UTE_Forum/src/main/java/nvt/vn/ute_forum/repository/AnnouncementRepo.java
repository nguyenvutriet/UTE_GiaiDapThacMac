package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, String> {
	Page<Announcement> findAllByOrderByIdDesc(Pageable pageable);

	@Query(value = "SELECT a FROM Announcement a " +
			"WHERE (:keyword IS NULL OR " +
			"LOWER(COALESCE(a.title, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"LOWER(COALESCE(a.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"LOWER(COALESCE(a.user.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
			"AND (:departmentId IS NULL OR a.user.department.id = :departmentId)",
			countQuery = "SELECT COUNT(a.id) FROM Announcement a " +
					"WHERE (:keyword IS NULL OR " +
					"LOWER(COALESCE(a.title, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
					"LOWER(COALESCE(a.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
					"LOWER(COALESCE(a.user.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
					"AND (:departmentId IS NULL OR a.user.department.id = :departmentId)")
	Page<Announcement> searchAnnouncements(@Param("keyword") String keyword,
									 @Param("departmentId") String departmentId,
									 Pageable pageable);
}
