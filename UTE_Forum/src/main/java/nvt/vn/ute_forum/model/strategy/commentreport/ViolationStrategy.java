package nvt.vn.ute_forum.model.strategy.commentreport;

import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.CommentReportService;

public class ViolationStrategy implements ReportActionStrategy {

    private final CommentReportService reportService;

    public ViolationStrategy(CommentReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(CommentReport report, Users admin) {

        Comment comment = report.getComment();

        if (comment != null) {
            reportService.deleteComment(comment, report, admin);
        } else {
            reportService.markDone(report, admin);
        }
    }
}