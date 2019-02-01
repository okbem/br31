
package okbem.br31.server.util.apphealth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;


@Component
public class AppHealth {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    @lombok.Getter
    private Health health;


    public AppHealth(
        @Value("${management.apphealth.init-status}")
        String status
    ) {
        this.change(status);
    }


    public void change(String status) {
        status = status.trim().toUpperCase();

        Health.Builder builder;

        switch (status) {
        case "DOWN":
            builder = Health.down();
            break;

        case "OUT_OF_SERVICE":
            builder = Health.outOfService();
            break;

        case "UP":
            builder = Health.up();
            break;

        default:
            throw new IllegalArgumentException(
                "Invalid status code: " +
                status + ". " +
                "The following status codes are currently available: " +
                "DOWN, OUT_OF_SERVICE, UP"
            );
        }

        this.health = builder
            .withDetail("date", LocalDateTime.now().toString())
            .build();

        logger.info("AppHealth changed to {}", status);
    }

}

