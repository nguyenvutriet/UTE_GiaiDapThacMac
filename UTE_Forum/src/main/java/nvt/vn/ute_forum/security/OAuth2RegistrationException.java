package nvt.vn.ute_forum.security;


import org.springframework.security.core.AuthenticationException;

public class OAuth2RegistrationException extends AuthenticationException {

    public OAuth2RegistrationException(String msg) {
        super(msg);
    }

}
