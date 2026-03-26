package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Request;
import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.repository.VoteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RequestService {

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private VoteRepo voteRepo;
    @Autowired
    private UsersRepo usersRepo;

    /**
     * Lấy các bài viết PUBLIC theo trang, kèm reaction, comment count
     * @param pageable phân trang
     * @param currentUserId id user hiện tại
     * @return Page<ForumPostDTO>
     */
    public Page<ForumPostDTO> getPublicPosts(Pageable pageable, String currentUserId) {
        return requestRepo.findByPostStatus("PUBLIC", pageable)
                .map(r -> {
                    ForumPostDTO dto = new ForumPostDTO();
                    dto.setId(r.getId());
                    dto.setSubject(r.getSubject());
                    dto.setDescription(r.getDescription());
                    dto.setDate(r.getTimeCreate());
                    dto.setStatus(r.getCurrentStatus());
                    dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
                    dto.setReactionTypeLower(dto.getReactionType() != null ? dto.getReactionType().toLowerCase() : "");
                    dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");
                    dto.setAttachments(mapAttachments(r));

                    dto.setCategories(r.getCategories().stream()
                            .map(c -> c.getSubject())
                            .collect(Collectors.toList()));

                    // Count comments
                    long commentCount = commentRepo.countByRequest_Id(r.getId());
                    dto.setCommentCount(commentCount);

                    // Lấy tất cả votes của bài viết
                    List<Vote> votes = voteRepo.findByRequest_Id(r.getId());

                    // Tính số lượng reactions cho 6 loại
                    Map<String, Long> reactionsMap = Map.of(
                            "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                            "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                            "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                            "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                            "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                            "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
                    );
                    dto.setReactions(reactionsMap);

                    // Tổng số reactions
                    long totalReactions = reactionsMap.values().stream().mapToLong(Long::longValue).sum();
                    dto.setTotalReactions(totalReactions);

                    // Reaction của user hiện tại
                    votes.stream()
                            .filter(v -> v.getUser().getId().equals(currentUserId))
                            .findFirst()
                            .ifPresent(v -> dto.setReactionType(v.getType().name()));

                    return dto;
                });
    }

    @Transactional
    public Map<String, Object> votePost(String requestId, String userId, ReactionType type) {
        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vote vote = voteRepo.findById_UserIdAndId_RequestId(userId, requestId).orElse(null);
        String currentType = "";

        if (vote != null) {
            if (vote.getType() == type) {
                voteRepo.delete(vote);
                voteRepo.flush(); // Xóa xong phải flush để tính tổng cho đúng
                currentType = ""; // Trạng thái sau khi Unvote
            } else {
                vote.setType(type);
                vote.setVoteAt(LocalDateTime.now());
                voteRepo.save(vote);
                currentType = type.name();
            }
        } else {
            vote = new Vote(new VoteId(userId, requestId), user, request, type, LocalDateTime.now());
            voteRepo.save(vote);
            currentType = type.name();
        }

        // Lấy tổng số lượng
        List<Vote> votes = voteRepo.findByRequest_Id(requestId);
        Map<String, Long> counts = new HashMap<>();
        for (ReactionType r : ReactionType.values()) counts.put(r.name(), 0L);
        votes.forEach(v -> counts.put(v.getType().name(), counts.get(v.getType().name()) + 1));

        // Trả về cả 2: Số lượng và Trạng thái của user này
        return Map.of("counts", counts, "currentType", currentType);
    }

    public List<ForumPostDTO> getFilteredPosts(String catId, String deptId, String sort, String currentUserId) {
        // Chuyển chuỗi rỗng thành null để câu Query IS NULL ở trên chạy đúng
        String cid = (catId == null || catId.trim().isEmpty()) ? null : catId;
        String did = (deptId == null || deptId.trim().isEmpty()) ? null : deptId;

        // Gọi Repo với các giá trị đã chuẩn hóa
        List<Request> entities = requestRepo.findByFilters(cid, did, sort);

        return entities.stream()
                .map(r -> convertToFullDTO(r, currentUserId))
                .collect(Collectors.toList());
    }

    // --- HÀM TÁI SỬ DỤNG ĐỂ CONVERT DỮ LIỆU ĐẦY ĐỦ ---
    private ForumPostDTO convertToFullDTO(Request r, String currentUserId) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(r.getId());
        dto.setSubject(r.getSubject());
        dto.setDescription(r.getDescription());
        dto.setDate(r.getTimeCreate()); // Dùng timeCreate cho khớp với getPublicPosts
        dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
        dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");
        dto.setAttachments(mapAttachments(r));

        // Categories
        dto.setCategories(r.getCategories().stream()
                .map(Category::getSubject)
                .collect(Collectors.toList()));

        // Count comments
        long commentCount = commentRepo.countByRequest_Id(r.getId());
        dto.setCommentCount(commentCount);

        // Lấy tất cả votes để tính toán Reactions
        List<Vote> votes = voteRepo.findByRequest_Id(r.getId());

        Map<String, Long> reactionsMap = Map.of(
                "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
        );
        dto.setReactions(reactionsMap);

        // Tổng số reactions
        long totalReactions = reactionsMap.values().stream().mapToLong(Long::longValue).sum();
        dto.setTotalReactions(totalReactions);

        // Reaction của user hiện tại (Để tô màu nút Like chẳng hạn)
        if (currentUserId != null) {
            votes.stream()
                    .filter(v -> v.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(v -> {
                        dto.setReactionType(v.getType().name());
                        dto.setReactionTypeLower(v.getType().name().toLowerCase());
                    });
        }

        return dto;
    }

    private List<ForumPostDTO.AttachmentDTO> mapAttachments(Request request) {
        if (request == null || request.getFileAttachments() == null || request.getFileAttachments().isEmpty()) {
            return Collections.emptyList();
        }

        return request.getFileAttachments().stream()
                .filter(file -> file != null && file.getFileUrl() != null && !file.getFileUrl().isBlank())
                .map(file -> {
                    ForumPostDTO.AttachmentDTO attachmentDTO = new ForumPostDTO.AttachmentDTO();
                    attachmentDTO.setFileName(file.getFileName() == null || file.getFileName().isBlank() ? "Tep dinh kem" : file.getFileName());
                    attachmentDTO.setFileUrl(file.getFileUrl());
                    attachmentDTO.setFileType(file.getFileType() == null ? "unknown" : file.getFileType());
                    return attachmentDTO;
                })
                .collect(Collectors.toList());
    }

    public List<Request> getRequestsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return requestRepo.findByUser_IdOrderByTimeCreateDesc(userId);
    }

    public Optional<Request> getRequestByIdAndUserId(String requestId, String userId) {
        if (requestId == null || requestId.isBlank() || userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return requestRepo.findByIdAndUser_Id(requestId, userId);
    }

    public Request saveOrUpdate(Request request) {
        return requestRepo.save(request);
    }

}

