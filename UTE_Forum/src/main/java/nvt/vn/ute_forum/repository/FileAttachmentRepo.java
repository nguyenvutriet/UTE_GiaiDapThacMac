package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAttachmentRepo extends JpaRepository<FileAttachment, String> {
	@Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 4) AS UNSIGNED)), 0) FROM fileattachment WHERE id REGEXP '^FA_[0-9]+$'", nativeQuery = true)
	Long findMaxFileAttachmentSequence();
}
