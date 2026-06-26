package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.model.builder.CommentBuilder;
import nvt.vn.ute_forum.model.composite.CommentComponent;
import nvt.vn.ute_forum.model.composite.CommentNode;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.CommentReportRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired private CommentRepo commentRepo;
    @Autowired private VoteCommentRepo voteCommentRepo;
    @Autowired private UsersService usersService;
    @Autowired private CommentReportRepo reportRepo;
    @Autowired private RequestRepo requestRepo;

    // =========================================================================
    // LẤY DANH SÁCH COMMENT — dùng Composite Pattern để build cây
    // =========================================================================

    /**
     * Trả về danh sách comment gốc, mỗi comment đã chứa sẵn replies lồng nhau.
     *
     * Chiến lược tránh N+1 query:
     *   1. Query 1 lần lấy tất cả comment gốc của request
     *   2. Query 1 lần lấy TẤT CẢ reply (parentId IN danh sách gốc)
     *   3. Build cây Composite trong memory — không query thêm
     */
    public List<CommentDTO> getCommentsByRequestId(String requestId, String currentUserId, boolean isAdmin) {

        // === Bước 1: Lấy comment gốc ===
        List<Comment> rootComments = isAdmin
                ? commentRepo.findRootByRequestId(requestId)
                : commentRepo.findActiveRootByRequestId(requestId);

        if (rootComments.isEmpty()) return Collections.emptyList();

        // === Bước 2: Lấy TẤT CẢ reply trong 1 query, nhóm theo parentId ===
        Set<String> rootIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());

        // Lấy replies theo từng parentId (1 query/root, nhưng chấp nhận được
        // vì số comment gốc thường ít; nếu cần tối ưu thêm thì dùng IN query)
        Map<String, List<Comment>> repliesByParentId = new HashMap<>();
        for (String rootId : rootIds) {
            List<Comment> replies = isAdmin
                    ? commentRepo.findAllRepliesByParentId(rootId)
                    : commentRepo.findActiveRepliesByParentId(rootId);
            repliesByParentId.put(rootId, replies);
        }

        // === Bước 3: Build cây Composite và chuyển sang DTO ===
        return rootComments.stream()
                .map(root -> {
                    CommentDTO rootDTO = buildDTO(root, currentUserId);
                    CommentComponent rootNode = new CommentNode(root, rootDTO);

                    // Thêm replies vào node gốc
                    List<Comment> replies = repliesByParentId.getOrDefault(root.getId(), Collections.emptyList());
                    buildReplyTree(rootNode, replies, currentUserId);

                    return rootNode.toDTO();
                })
                .collect(Collectors.toList());
    }

    /**
     * Build cây reply đệ quy theo replyId (cấp trực tiếp).
     *
     * Mặc dù tất cả reply cùng parentId, chúng vẫn có thể lồng nhau
     * theo replyId. VD: A→B→C: B.replyId=A.id, C.replyId=B.id
     */
    private void buildReplyTree(CommentComponent parent, List<Comment> allReplies, String currentUserId) {
        String parentId = parent.getId();

        allReplies.stream()
                .filter(r -> parentId.equals(r.getReplyId())) // Con trực tiếp
                .forEach(reply -> {
                    CommentDTO replyDTO = buildDTO(reply, currentUserId);
                    CommentComponent replyNode = new CommentNode(reply, replyDTO);

                    // Đệ quy: tìm con của reply này
                    buildReplyTree(replyNode, allReplies, currentUserId);

                    parent.addChild(replyNode);
                });
    }

    /**
     * Chuyển 1 Comment entity → CommentDTO, điền đầy đủ thông tin
     * (reaction, report, canDelete, reply fields...)
     */
    private CommentDTO buildDTO(Comment c, String currentUserId) {
        String commentOwnerId = c.getUser() != null ? String.valueOf(c.getUser().getId()) : null;
        boolean canDelete = currentUserId != null && currentUserId.equals(commentOwnerId);

        CommentDTO dto = new CommentDTO(
                c.getUser() != null ? c.getUser().getFullName() : "Ẩn danh",
                c.getContent(),
                c.getDate(),
                c.getId(),
                canDelete,
                c.getUser() != null ? c.getUser().getRole() : "ROLE_STUDENT"
        );

        // Điền reply fields
        dto.setParentId(c.getParentId());
        dto.setReplyId(c.getReplyId());
        dto.setReplyToUserId(c.getReplyToUserId());
        dto.setReplyToUserName(c.getReplyToUserName());

        // Reaction của user hiện tại
        Optional<VoteComment> userVote = Optional.empty();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            userVote = voteCommentRepo.findByIdUserIdAndIdCommentId(currentUserId, c.getId());
        }
        dto.setReactionType(userVote.map(v -> v.getType().name()).orElse(null));

        // Tổng hợp reaction theo loại
        List<Object[]> raw = voteCommentRepo.countReactionsByCommentId(c.getId());
        Map<String, Long> reactions = new HashMap<>();
        for (Object[] row : raw) {
            reactions.put(row[0].toString(), (Long) row[1]);
        }
        dto.setReactions(reactions);

        // Kiểm tra đã report chưa
        boolean reportedByCurrentUser = false;
        if (currentUserId != null && !currentUserId.isEmpty()) {
            reportedByCurrentUser = reportRepo.existsByComment_IdAndStudent_Id(c.getId(), currentUserId);
        }
        dto.setReportedByCurrentUser(reportedByCurrentUser);
        dto.setIsActive(c.getIsActive());

        return dto;
    }

    // =========================================================================
    // LƯU COMMENT — dùng Builder Pattern
    // =========================================================================

    /**
     * Lưu comment gốc (top-level) — không có parentId/replyId
     */
    @Transactional
    public Comment saveComment(String requestId, Users user, String content) {
        Request postRequest = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        Comment comment = new CommentBuilder(postRequest, user, content)
                .build();

        return commentRepo.save(comment);
    }

    /**
     * Lưu reply — dùng Builder để thiết lập parentId + replyId + @mention
     *
     * @param requestId   ID của bài viết
     * @param user        User đang reply
     * @param content     Nội dung reply
     * @param parentId    ID comment gốc của thread (flat thread: dùng khi replyId trỏ về comment gốc)
     * @param replyId     ID comment được reply trực tiếp
     */
    @Transactional
    public Comment saveReply(String requestId, Users user, String content,
                             String parentId, String replyId) {
        Request postRequest = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        // Lấy comment được reply trực tiếp để lấy thông tin @mention
        Comment replyTargetComment = commentRepo.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment để reply!"));

        // parentId trong JPA relationship = comment cấp ngay trên (replyId)
        Comment parentCommentEntity = replyTargetComment;

        // User được mention = user sở hữu comment được reply
        Users replyToUser = replyTargetComment.getUser();

        // Xác định parentId thực sự:
        // Nếu comment được reply đã có parentId → dùng parentId đó (giữ flat)
        // Nếu không → comment được reply chính là gốc, dùng replyId làm parentId
        String resolvedParentId = (replyTargetComment.getParentId() != null)
                ? replyTargetComment.getParentId()
                : replyId;

        Comment comment = new CommentBuilder(postRequest, user, content)
                .asReply(resolvedParentId, replyId, replyToUser, parentCommentEntity)
                .build();

        return commentRepo.save(comment);
    }

    // =========================================================================
    // XÓA COMMENT
    // =========================================================================

    @Transactional
    public boolean deleteCommentIfOwner(String commentId, String currentUserId) {
        return commentRepo.findById(commentId).map(comment -> {
            String ownerId = String.valueOf(comment.getUser().getId()).trim();
            String visitorId = currentUserId.trim();
            if (ownerId.equals(visitorId)) {
                commentRepo.delete(comment);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public void deleteCommentAsAdmin(String commentId) {
        commentRepo.findById(commentId).ifPresent(c -> {
            c.setIsActive(false);
            commentRepo.save(c);
        });
    }

    // =========================================================================
    // TIỆN ÍCH
    // =========================================================================

    public Comment getCommentById(String commentId) {
        return commentRepo.findById(commentId).orElse(null);
    }

    @Transactional
    public void saveCommentReport(CommentReport report) {
        reportRepo.save(report);
    }

    @Transactional
    public Comment save(Comment comment) {
        return commentRepo.save(comment);
    }
}