package nvt.vn.ute_forum.model.factory;

import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.CommentReport;
import nvt.vn.ute_forum.model.Users;

import java.time.LocalDateTime;
import java.util.UUID;

public class DefaultCommentReportFactory
        extends CommentReportFactory {

    @Override
    public CommentReportProduct factoryMethod(
            Comment comment,
            Users student,
            String reason) {

        CommentReport report = new CommentReport();

        report.setId(UUID.randomUUID().toString());

        report.setComment(comment);

        report.setStudent(student);

        report.setReason(reason);

        report.setStatus("pending");

        report.setCreatedAt(LocalDateTime.now());

        report.setAdmin(null);

        return report;
    }
}