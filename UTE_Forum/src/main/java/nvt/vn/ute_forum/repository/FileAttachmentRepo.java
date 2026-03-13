package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAttachmentRepo extends JpaRepository<FileAttachment, String> {
}
