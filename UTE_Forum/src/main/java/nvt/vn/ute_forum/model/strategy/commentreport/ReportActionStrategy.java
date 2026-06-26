package nvt.vn.ute_forum.model.strategy.commentreport;

import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;

public interface ReportActionStrategy {

    void execute(CommentReport report, Users admin);

}