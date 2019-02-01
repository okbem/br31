
package okbem.br31.server.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
public class JwtManager {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    private final String issuer;


    private final Algorithm algorithm;


    private final JWTVerifier jwtVerifier;


    private final Duration expiration;


    private final UserAuthService userAuthService;


    public JwtManager(
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") Duration expiration,
        UserAuthService userAuthService
    ) {
        this.issuer = issuer;

        this.algorithm = Algorithm.HMAC512(secret);

        this.jwtVerifier = JWT.require(this.algorithm)
            .withIssuer(this.issuer)
            .build();

        this.expiration = expiration;

        this.userAuthService = userAuthService;

        logger.info("issuer={}, algorithm={}, expiration={}",
            this.issuer,
            this.algorithm,
            this.expiration
        );
    }


    public String encode(UserDetails userDetails) throws JWTCreationException {
        Date exp = Date.from(Instant.now().plus(this.expiration));
        String user = userDetails.getUsername();
        String[] role = userDetails.getAuthorities().stream()
            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toList())
            .toArray(new String[0]);
        Long rev = this.userAuthService.getUserRevisionFromCache(user);

        if (rev == null)
            rev = -1L;

        return JWT.create()
            .withIssuer(this.issuer)
            .withExpiresAt(exp)
            .withClaim("user", user)
            .withArrayClaim("role", role)
            .withClaim("rev", rev)
            .sign(this.algorithm);
    }


    public UserDetails decode(String token) throws JWTVerificationException {
        DecodedJWT jwt = this.jwtVerifier.verify(token);

        String user = jwt.getClaim("user").asString();
        String[] role = jwt.getClaim("role").asArray(String.class);
        Long rev = jwt.getClaim("rev").asLong();

        if (user == null || role == null || role.length == 0 || rev == null)
            throw new InvalidClaimException(String.format(
                "user=%s, role=%s, rev=%s",
                user,
                role,
                rev
            ));

        Long minRequiredRevision
            = this.userAuthService.getUserRevisionFromCache(user);

        if (minRequiredRevision == null || rev < minRequiredRevision)
            throw new TokenExpiredException(String.format(
                "rev=%s, minRequiredRevision=%s",
                rev,
                minRequiredRevision
            ));

        return org.springframework.security.core.userdetails.User
            .withUsername(user)
            .password("")
            .roles(role)
            .build();
    }

}

