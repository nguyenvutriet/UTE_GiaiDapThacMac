package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.EmailService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

@Controller
public class LoginController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private EmailService emailService;


    @GetMapping("/login")
    public String login(){
        return "Login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(@RequestParam (required = false) String error, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }

        return "ForgotPassword";
    }

    @GetMapping("/send-otp")
    public String sendOTP(@RequestParam String email, RedirectAttributes model){

        Users user = usersService.getByEmail(email);

        if(user == null){
            model.addFlashAttribute("error", "Email không tồn tại");
            return "redirect:/forgot-password";
        }

        Context context = new Context();
        context.setVariable("fullName", user.getFullName());
        context.setVariable("otp", emailService.getOtp());

        emailService.sendOtpAdmin(email, context, "otp-email");

        model.addAttribute("user", user);

        return "/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam (required = false) String error, @RequestParam Users user, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }
        model.addAttribute("user", user);
        return "VerifyOtp";
    }



    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model){
        if(!emailService.verifyOtp(otp)){
            model.addAttribute("user", usersService.getByEmail(email));
            return "redirect:/verify-otp?error=OTP không hợp lệ";
        }


        model.addAttribute("user", usersService.getByEmail(email));
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam (required = false) String error, @RequestParam Users user, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }

        model.addAttribute("user", user);
        return "ResetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String password,  Model model){
        if(password.length() < 6){
            model.addAttribute("user", usersService.getByEmail(email));
            return "redirect:/reset-password?error=Mật khẩu phải có ít nhất 6 ký tự";
        }

        if(!usersService.overLapByPassword(password, email)){
            model.addAttribute("user", usersService.getByEmail(email));
            return "redirect:/reset-password?error=Mật khẩu mới không được trùng với mật khẩu cũ";
        }

        if(usersService.existPassword(password, email)){
            model.addAttribute("user", usersService.getByEmail(email));
            return "redirect:/reset-password?error=Mật khẩu đã tồn tại, vui lòng chọn mật khẩu khác";
        }

        usersService.updateUser(email, password);
        usersService.loadUserByUsername(email);

        return "redirect:/login";
    }








}
