
package okbem.br31.server.util.apphealth;

import javax.annotation.Resource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class AppHealthIndicator implements HealthIndicator {

    @Resource
    private AppHealth appHealth;


    @Override
    public Health health() {
        return this.appHealth.getHealth();
    }

}

