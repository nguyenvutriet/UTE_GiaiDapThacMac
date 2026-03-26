package nvt.vn.ute_forum.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    private final String ADMIN_GMAIL_ID = "nguyenvutriet0205@gmail.com";

    private String lastOTP;

    private Instant otpTimeStamp;

    public String getOtp(){

        String otp = String.format("%06d", new Random().nextInt(100000));

        this.lastOTP = otp;
        this.otpTimeStamp = Instant.now();

        return otp;
    }

    public void sendOtpAdmin(String email, Context context, String templateName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String html = templateEngine.process(templateName, context);

            helper.setTo(email);
            helper.setFrom(ADMIN_GMAIL_ID);
            helper.setText(html, true);

            mailSender.send(message);
        }catch (org.springframework.mail.MailAuthenticationException e) {
            System.err.println("[EMAIL ERROR] Lỗi xác thực SMTP - Kiểm tra email và app password!");
            System.err.println("[EMAIL ERROR] " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Lỗi gửi email: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean verifyOtp(String otp){

        int time = Math.toIntExact(Instant.now().getEpochSecond() - otpTimeStamp.getEpochSecond());

        if(time > 90){
            return false;
        }

        return otp.equals(lastOTP);
    }


}
