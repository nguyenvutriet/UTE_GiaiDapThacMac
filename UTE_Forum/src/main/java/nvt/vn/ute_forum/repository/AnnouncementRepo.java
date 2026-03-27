package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Announcement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AnnouncementRepo extends JpaRepository<Announcement, String>,
        JpaSpecificationExecutor<Announcement> {
    @Query("SELECT a FROM Announcement a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(a.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Announcement> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
