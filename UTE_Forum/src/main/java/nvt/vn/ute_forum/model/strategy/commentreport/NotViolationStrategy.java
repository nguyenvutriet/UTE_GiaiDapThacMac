package nvt.vn.ute_forum.model.strategy.commentreport;

import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.CommentReportService;

public class NotViolationStrategy implements ReportActionStrategy {

    private final CommentReportService reportService;

    public NotViolationStrategy(CommentReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(CommentReport report, Users admin) {

        reportService.markDone(report, admin);

    }
}