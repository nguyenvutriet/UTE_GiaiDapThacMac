package nvt.vn.ute_forum.security;

import nvt.vn.ute_forum.model.UserPrincipal;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.CustomOAuth2UserService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;



    @Autowired
    private UsersService usersService;

    @Bean
    public AuthenticationProvider authenicationProvider(BCryptPasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordBcypt(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFiltercChain(HttpSecurity http) throws Exception {

        http.csrf(request -> request.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/forgot-password",
                                "/send-otp",
                                "/verify-otp",
                                "/reset-password",
                                "/verify-otp",
                                "/api/forum/**",
                                "/api/comments/**",
                                "/api/vote-comment/**",

                                "/feedback/send",
                                "/apoi/department/**",

                                "/api/forum/reactors/details",
                                "/api/forum/search",
                                "/ws",
                                "/topic/**",
                                "/app/**",
                                "/department/**",
                                "/api/history/chat/upload",
                                "/clarification/send"
                        ).permitAll()
                        .requestMatchers("/admin/forum/**", "/adannouncement/**", "/api/announcement/**", "/api/departments/** ",
                                "/admin/category", "/admin/category/search", "/admin/category/create", "/admin/category/update",
                                "/admin/category/activate/**", "/admin/category/deactivate/**", "/admin/reports",
                                "/admin/reports/**", "/admin/list-feedbacks", "/admin/feedback-detail", "/admin/search-feedbacks",
                                "/admin/filter-feedbacks", "/admin/forum/view", "/admin/forum/*", "/admin/forum/search-list",
                                "/api/comments/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/list-feedbacks", "/api/forum/staff", "/staff/create-conversation",
                                "/chat.send/*", "/staff/close-conversation/*", "/staff/conversation/*/messages", "/staff/feedback-detail",
                                "/staff/search-feedbacks", "/staff/filter-feedbacks", "/staff/update-status", "/staff/forward", "/api/forum/staff/**").hasRole("DEPARTMENT")
                        .requestMatchers("/api/forum/view", "/feedback/send", "/api/submit", "/api/edit-request",
                                "/api/delete-request", "/feedback/send", "/api/update-request", "/api/history/**", "/api/forum/*",
                                "/api/forum/search-list", "/announcement", "/announcement/detail/**", "/api/announcement/**", "/api/departments/** ").hasRole("STUDENT")
                        .anyRequest().authenticated())
                .formLogin(
                        form ->  form.loginPage("/login")
                                .loginProcessingUrl("/process-login")
                                .usernameParameter("email")
                                .successHandler(successHandler())
                                .failureHandler(formFailureHandler())
                                .permitAll()
                )
                .oauth2Login(
                        oauth -> oauth.loginPage("/login")
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(oAuth2UserService())
                                )
                                .successHandler(oauth2UrlHandler())
                                .failureHandler(oAuth2FailureHandler())
                )
                .logout(
                        logout -> logout.logoutUrl("/logout")
                                .logoutSuccessUrl("/login")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );


        return http.build();
    }

    public AuthenticationSuccessHandler oauth2UrlHandler() {
        return (request, response, authentication) -> {

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");

            Users user = usersService.getByEmail(email);

            if (user == null) {
                response.sendRedirect("/login?oauth_error=not_found");
                return;
            }

            UserPrincipal userPrincipal = new UserPrincipal(user);

            Authentication newAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(newAuth);
            request.getSession().setMaxInactiveInterval(0);

            if(userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/list-feedbacks");
            }else if(userPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT"))){
                response.sendRedirect("/staff/list-feedbacks");
            }else{
                response.sendRedirect("/api/forum/view");
            }
        };
    }

    public AuthenticationFailureHandler oAuth2FailureHandler() {
        return (request, response, exception) -> {

            if (exception instanceof OAuth2RegistrationException) {

                String message = exception.getMessage();

                if (message.equals("Tài khoản không tồn tại")) {
                    response.sendRedirect("/login?oauth_error=not_found");
                    return;
                }
            }

            response.sendRedirect("/login?oauth_error=true");
        };
    }

    private AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/list-feedbacks");
            }else if(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT"))){
                response.sendRedirect("/staff/list-feedbacks");
            } else {
                response.sendRedirect("/api/forum/view");
            }
        };
    }

    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return new CustomOAuth2UserService();
    }

    public AuthenticationFailureHandler formFailureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("/login?error=true");
        };
    }




}
