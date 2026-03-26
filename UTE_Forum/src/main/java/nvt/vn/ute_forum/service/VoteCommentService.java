package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.ReactionUserDTO;
import nvt.vn.ute_forum.dto.VoteResponse;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.UsersRepo;
import nvt.vn.ute_forum.repository.VoteCommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VoteCommentService {

    @Autowired
    private VoteCommentRepo voteCommentRepository;
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private CommentRepo commentRepo;

    @Transactional
    public VoteResponse toggleReaction(String username, String commentId, ReactionType type) {
        // 1. Lấy thông tin User từ DB (Dùng username từ Principal)
        Users user = usersRepo.findOptionalByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User: " + username));

        // 2. Lấy thông tin Comment từ DB
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Comment ID: " + commentId));

        // 3. TẠO ID Ở ĐÂY: Lấy chuỗi ID String của User và Comment ghép vào
        // Đây chính là lúc bạn "tạo" dãy String ID cho bản ghi Vote
        VoteCommentId id = new VoteCommentId(user.getId(), comment.getId());

        Optional<VoteComment> existing = voteCommentRepository.findById(id);

        boolean isVoted;
        String finalType = type.name();

        if (existing.isPresent()) {
            VoteComment vote = existing.get();
            if (vote.getType() == type) {
                // Nếu bấm lại icon cũ -> Xóa (Unlike)
                voteCommentRepository.delete(vote);
                isVoted = false;
                finalType = null;
            } else {
                // Nếu bấm icon khác -> Cập nhật loại cảm xúc
                vote.setType(type);
                vote.setVoteAt(LocalDateTime.now());
                voteCommentRepository.save(vote);
                isVoted = true;
            }
        } else {
            // 4. Nếu chưa có -> Tạo mới bản ghi VoteComment
            VoteComment newVote = new VoteComment();
            newVote.setId(id); // Gán dãy String ID vừa tạo ở trên vào
            newVote.setUser(user);
            newVote.setComment(comment);
            newVote.setType(type);
            newVote.setVoteAt(LocalDateTime.now());

            voteCommentRepository.save(newVote);
            isVoted = true;
        }

        long count = voteCommentRepository.countByIdCommentId(commentId);

// 🔥 LẤY TỪNG LOẠI REACTION
        List<Object[]> raw = voteCommentRepository.countReactionsByCommentId(commentId);

        Map<String, Long> reactions = new HashMap<>();
        for (Object[] row : raw) {
            reactions.put(row[0].toString(), (Long) row[1]);
        }

// 🔥 TRẢ FULL DATA
        return new VoteResponse(isVoted, finalType, count, reactions);
    }
    public List<ReactionUserDTO> getReactionUsers(String commentId) {
        return voteCommentRepository.findAllByCommentId(commentId)
                .stream()
                .map(row -> new ReactionUserDTO(
                        String.valueOf(row[0]),
                        (String) row[1],
                        row[2].toString(),
                        row[3].toString()           // userRole 🔥 Lấy trực tiếp giá trị DB
                ))
                .toList();
    }
}