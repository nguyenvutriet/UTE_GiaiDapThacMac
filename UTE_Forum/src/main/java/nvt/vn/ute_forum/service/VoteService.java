package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.ReactionDetailDTO;
import nvt.vn.ute_forum.repository.VoteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoteService {
    @Autowired
    private VoteRepo voteRepository;

    public List<ReactionDetailDTO> getReactionDetails(String requestId) {
        return voteRepository.findReactionDetails(requestId);
    }

}
