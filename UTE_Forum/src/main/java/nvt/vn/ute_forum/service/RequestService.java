package nvt.vn.ute_forum.service;

import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.repository.VoteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                    dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
                    dto.setReactionTypeLower(dto.getReactionType() != null ? dto.getReactionType().toLowerCase() : "");
                    dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");

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
    public Map<String, Long> votePost(String requestId, String userId, ReactionType type) {
        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm vote cũ
        Vote vote = voteRepo.findById_UserIdAndId_RequestId(userId, requestId).orElse(null);

        if (vote != null) {
            if (vote.getType() == type) {
                // Nếu user click lại reaction cũ → unreact
                voteRepo.delete(vote);
            } else {
                // Nếu user đổi reaction
                vote.setType(type);
                vote.setVoteAt(LocalDateTime.now());
                voteRepo.save(vote);
            }
        } else {
            // Nếu chưa vote → tạo vote mới
            vote = new Vote(new VoteId(userId, requestId), user, request, type, LocalDateTime.now());
            voteRepo.save(vote);
        }

        // Tính tổng reactions
        List<Vote> votes = voteRepo.findByRequest_Id(requestId);
        return Map.of(
                "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
        );
    }

}