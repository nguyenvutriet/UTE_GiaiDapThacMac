package nvt.vn.ute_forum.command;

import nvt.vn.ute_forum.service.EmailService;
import org.thymeleaf.context.Context;

public class SendEmailCommand implements ICommand{

    private EmailService emailService;
    private String email;
    private Context context;
    private String template;

    public SendEmailCommand(EmailService emailService, String email, Context context, String template) {
        this.emailService = emailService;
        this.email = email;
        this.context = context;
        this.template = template;
    }

    @Override
    public void execute() {
        emailService.sendOtpAdmin(email, context, template);
    }
}
