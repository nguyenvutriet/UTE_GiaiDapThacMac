package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RequestService {
    @Autowired
    private RequestRepo requestRepo;
    @Autowired
    private CommentRepo commentRepo; // Thêm cái này

    public Page<ForumPostDTO> getPublicPosts(Pageable pageable) {
        // Tìm bài viết PUBLIC theo phân trang
        return requestRepo.findByPostStatus("PUBLIC", pageable)
                .map(r -> {
                    ForumPostDTO dto = new ForumPostDTO();
                    dto.setId(r.getId());
                    dto.setSubject(r.getSubject());
                    dto.setDescription(r.getDescription());
                    dto.setDate(r.getTimeCreate()); // LocalDateTime từ Entity

                    dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
                    dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");

                    dto.setCategories(r.getCategories().stream()
                            .map(c -> c.getSubject())
                            .collect(Collectors.toList()));
                    // CHỈNH Ở ĐÂY: Đếm số lượng comment thực tế từ DB
                    long count = commentRepo.countByRequest_Id(r.getId());
                    dto.setCommentCount(count);
                    return dto;
                });
    }


}