
package okbem.br31.server.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserAuthService implements UserDetailsService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    @lombok.Data
    private static final class User {

        private final String username;
        private final String password;
        private final java.util.List<String> roleList;
        private final Long revision;

        public static User create(String username) {
            return new User(
                username,
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode(username),
                java.util.Arrays.asList("USER"),
                0L
            );
        }

    }


    private final LoadingCache<String, Long> userRevisionCache;


    public UserAuthService(
        @Value("${caffeine.spec.user-revision}") String spec
    ) {
        this.userRevisionCache = Caffeine.from(spec)
            .build(this::getUserRevision);

        logger.info("userRevisionCache initialized: spec={}", spec);
    }


    private Long getUserRevision(String username) {
        User user = User.create(username);

        if (user == null)
            return null;

        return user.getRevision();
    }


    public Long getUserRevisionFromCache(String username) {
        return this.userRevisionCache.get(username);
    }


    @Override
    public UserDetails loadUserByUsername(
        String username
    ) throws UsernameNotFoundException {
        User user = User.create(username);

        if (user == null)
            throw new UsernameNotFoundException(username);

        this.userRevisionCache.put(username, user.getRevision());

        return org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password(user.getPassword())
            .roles(user.getRoleList().toArray(new String[0]))
            .build();
    }

}

