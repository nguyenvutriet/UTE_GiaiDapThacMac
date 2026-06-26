package nvt.vn.ute_forum.model.factory;

import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Users;

public abstract class CommentReportFactory {

    public abstract CommentReportProduct factoryMethod(

            Comment comment,

            Users student,

            String reason

    );

}