package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService implements UserDetailsService {

    @Autowired
    private UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Users user = usersRepo.findByEmail(email);

        if(user == null){
            throw new UsernameNotFoundException("Tài khoản không tồn tại");
        }

        return new UserPrincipal(user);
    }

    public Users getByEmail(String email){
        return usersRepo.findByEmail(email);
    }

    public boolean overLapByPassword(String password, String email){
        Users user = usersRepo.findByEmail(email);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return passwordEncoder.matches(password, user.getPassword());
    }

    public boolean existPassword(String password, String email){
        for(Users user :  usersRepo.findAll()){
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if(passwordEncoder.matches(password, user.getPassword()) && !user.getEmail().equals(email)){
                return true;
            }
        }
        return false;
    }

    public void updateUser(String email, String password){
        Users user = usersRepo.findByEmail(email);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        user.setPassword(passwordEncoder.encode(password));

        usersRepo.save(user);
    }

    public Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        // Nếu dùng UserPrincipal
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUser();  // lấy entity Users
        }

        // Nếu dùng mặc định tạo bởi Spring Security (email)
        String email = auth.getName();
        return getByEmail(email);
    }
}
