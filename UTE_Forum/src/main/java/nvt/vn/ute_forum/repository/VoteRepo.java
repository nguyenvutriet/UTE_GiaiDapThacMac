package nvt.vn.ute_forum.repository;

import nvt.vn.ute_forum.dto.ReactionDetailDTO;
import nvt.vn.ute_forum.model.Vote;
import nvt.vn.ute_forum.model.VoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepo extends JpaRepository<Vote, VoteId> {
    List<Vote> findByRequest_Id(String requestId);
    Optional<Vote> findById_UserIdAndId_RequestId(String userId, String requestId);
    @Query("SELECT new nvt.vn.ute_forum.dto.ReactionDetailDTO(u.id, u.fullName, v.type, u.role) " +
            "FROM Vote v " +
            "JOIN v.user u " +
            "WHERE v.id.requestId = :requestId " +
            "ORDER BY v.voteAt DESC")
    List<ReactionDetailDTO> findReactionDetails(@Param("requestId") String requestId);
}
