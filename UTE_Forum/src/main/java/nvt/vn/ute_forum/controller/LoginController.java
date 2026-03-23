package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.EmailService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        model.addFlashAttribute("user", user);

        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam (required = false) String error, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }
        return "VerifyOtp";
    }



    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email , @RequestParam String otp, RedirectAttributes redirectAttributes){
        if(!emailService.verifyOtp(otp)){
            redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
            redirectAttributes.addFlashAttribute("error", "OTP không hợp lệ");
            return "redirect:/verify-otp";
        }


        redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam (required = false) String error, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }

        return "ResetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String password, RedirectAttributes redirectAttributes, Model model){
        if(password.length() < 6){
            redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
            return "redirect:/reset-password";
        }

        if(usersService.overLapByPassword(password, email)){
            redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được trùng với mật khẩu cũ");
            return "redirect:/reset-password";
        }

        if(usersService.existPassword(password, email)){
            redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
            redirectAttributes.addFlashAttribute("error", "Mật khẩu đã tồn tại, vui lòng chọn mật khẩu khác");
            return "redirect:/reset-password";
        }

        usersService.updateUser(email, password);
        usersService.loadUserByUsername(email);

        redirectAttributes.addFlashAttribute("user", usersService.getByEmail(email));
        return "redirect:/login";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam (required = false) String error, Model model){

        if(error != null){
            model.addAttribute("error", error);
        }

        model.addAttribute("user", usersService.getByEmail(userPrincipal.getUsername()));

        return "ChangePassword";
    }











}
