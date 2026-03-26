package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.CommentReportDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.CommentReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentReportService {

    @Autowired
    private CommentReportRepo commentReportRepo;

    @Autowired
    private CommentReportRepo reportRepo;

    @Autowired
    private CommentService commentService;

    // Chỉ đánh dấu report đã xử lý
    public void markDone(CommentReport report, Users admin) {
        report.setStatus("done");
        report.setAdmin(admin);
        reportRepo.save(report);
    }

    // Xóa comment + đánh dấu report done
    public void deleteComment(Comment comment, CommentReport report, Users admin) {

        // Ẩn comment (không xóa)
        comment.setIsActive(false);
        commentService.save(comment);

        // Đánh dấu report đã xử lý
        report.setStatus("done");
        report.setAdmin(admin);
        reportRepo.save(report);
    }

    public Optional<CommentReport> findById(String reportId) {
        return commentReportRepo.findById(reportId);
    }
    /**
     * Lấy tất cả report (JOIN comment + student) rồi map sang DTO
     */
    public List<CommentReportDTO> findAllDTO() {

        return commentReportRepo.findAllWithComment()
                .stream()
                .map(r -> {

                    Comment c = r.getComment();

                    String commentContent = (c != null) ? c.getContent() : "";
                    String studentName = (r.getStudent() != null) ? r.getStudent().getFullName() : "Unknown";

                    String adminName = (r.getAdmin() != null)
                            ? r.getAdmin().getFullName()
                            : "Chưa duyệt";

                    String requestId = (c != null && c.getRequest() != null)
                            ? c.getRequest().getId()
                            : "";

                    String commentId = (c != null)
                            ? c.getId()
                            : "";

                    Boolean active = (c != null) ? c.getIsActive() : null;

                    return new CommentReportDTO(
                            r.getId(),
                            commentContent,
                            r.getReason(),
                            studentName,
                            r.getStatus(),
                            requestId,
                            commentId,
                            r.getCreatedAt(),
                            adminName,
                            active
                    );
                })
                .toList();
    }


    /**
     * Admin duyệt report → đổi trạng thái thành 'done'
     */
    public void updateStatus(String reportId, Users admin) {

        commentReportRepo.findById(reportId).ifPresent(r -> {
            r.setStatus("done");
            r.setAdmin(admin); // cập nhật admin xử lý nếu cần
            commentReportRepo.save(r);
        });
    }


    /**
     * Lọc theo keyword + trạng thái
     */
    public List<CommentReportDTO> filter(List<CommentReportDTO> list, String keyword, String status) {

        // Lọc theo từ khóa trong nội dung comment
        if (keyword != null && !keyword.isEmpty()) {
            String key = keyword.toLowerCase();
            list = list.stream()
                    .filter(r -> r.getCommentContent() != null &&
                            r.getCommentContent().toLowerCase().contains(key))
                    .toList();
        }

        // Lọc theo trạng thái
        if ("pending".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(r -> "pending".equalsIgnoreCase(r.getStatus()))
                    .toList();
        }

        if ("done".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(r -> "done".equalsIgnoreCase(r.getStatus()))
                    .toList();
        }

        return list;
    }
}
