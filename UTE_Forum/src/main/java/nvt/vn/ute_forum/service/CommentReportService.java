package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.CommentReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentReportService {

    @Autowired
    private CommentReportRepo commentReportRepo;

}
