
package okbem.br31.server.security;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginRequestBody {

        @NotBlank
        private String username;

        @NotBlank
        private String password;

    }


    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginResponseBody {

        @NotBlank
        private String token;

    }


    @Resource
    private AuthenticationManager authenticationManager;


    @Resource
    private JwtManager jwtManager;


    @PostMapping("/login")
    public LoginResponseBody login(
        @RequestBody @Valid LoginRequestBody reqBody
    ) {
        Authentication authentication = this.authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                reqBody.username,
                reqBody.password
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails)authentication.getPrincipal();

        return new LoginResponseBody(this.jwtManager.encode(userDetails));
    }

}

