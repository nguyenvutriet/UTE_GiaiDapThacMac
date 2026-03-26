package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository

public interface AnnouncementRepo extends JpaRepository<Announcement, String>,
        JpaSpecificationExecutor<Announcement> { // Kế thừa thêm ở đây
}
