
package okbem.br31.server.util.apphealth;

import javax.annotation.Resource;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;


@Component
@Endpoint(id="apphealth")
public class AppHealthEndpoint {

    @Resource
    private AppHealth appHealth;


    @WriteOperation
    public void post(@Selector String status) {
        this.appHealth.change(status);
    }

}

