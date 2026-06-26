package nvt.vn.ute_forum.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class SpringMailAdapter implements IMailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void send(String emailAdmin, String email, Context context, String templateName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String html = templateEngine.process(templateName, context);

            helper.setTo(email);
            helper.setFrom(emailAdmin);
            helper.setText(html, true);

            mailSender.send(message);
        }catch (MailAuthenticationException e) {
            System.err.println("[EMAIL ERROR] Lỗi xác thực SMTP - Kiểm tra email và app password!");
            System.err.println("[EMAIL ERROR] " + e.getMessage());
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}
