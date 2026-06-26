package nvt.vn.ute_forum.model.strategy.commentreport;

import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;

public class ReportActionContext {

    private ReportActionStrategy strategy;

    public void setStrategy(ReportActionStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute(CommentReport report, Users admin) {
        strategy.execute(report, admin);
    }

}