package nvt.vn.ute_forum.adapter;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
public interface IMailSender {
    public void send(String emailAdmin, String email, Context context, String templateName);
}
