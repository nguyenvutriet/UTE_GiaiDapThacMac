package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepo extends JpaRepository<Request, String> {
    // Thêm Pageable vào đây và đổi kiểu trả về thành Page<Request>
    Page<Request> findByPostStatus(String status, Pageable pageable);

}
