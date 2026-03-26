package nvt.vn.ute_forum.controller;

import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.NotificationService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class StudentLayoutModelAdvice {

    private final UsersService usersService;
    private final NotificationService notificationService;

    public StudentLayoutModelAdvice(UsersService usersService, NotificationService notificationService) {
        this.usersService = usersService;
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(Authentication authentication) {
        Users user = resolveAuthenticatedUser(authentication);
        if (user == null) {
            return 0;
        }
        return notificationService.countUnreadByUserId(user.getId());
    }

    private Users resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof UserPrincipal userPrincipal) {
            email = userPrincipal.getUsername();
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalName) {
            email = principalName;
        }

        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return null;
        }

        return usersService.getByEmail(email.trim());
    }
}

