package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, String> {
	Page<Announcement> findAllByOrderByIdDesc(Pageable pageable);
}
