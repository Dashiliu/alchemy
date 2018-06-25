package com.dfire.platform.alchemy.web.config.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.Assert;

@Endpoint(id = "check_health")
public class CheckHealthEndpoint {

    private final HealthIndicator healthIndicator;

    public CheckHealthEndpoint(HealthIndicator healthIndicator) {
        Assert.notNull(healthIndicator, "HealthIndicator must not be null");
        this.healthIndicator = healthIndicator;
    }

    @ReadOperation
    public String health() {
        Health health = this.healthIndicator.health();
        if (health.getStatus() != null && health.getStatus().equals(Status.UP)) {
            return "ok";
        } else {
            return health.toString();
        }
    }
}
