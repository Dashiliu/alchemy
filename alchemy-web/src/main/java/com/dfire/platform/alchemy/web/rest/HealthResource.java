package com.dfire.platform.alchemy.web.rest;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/check_health")
public class HealthResource {

    private final HealthIndicator[] healthIndicators;

    public HealthResource(HealthIndicator... healthIndicators) {
        this.healthIndicators = healthIndicators;
    }

    @GetMapping
    @ResponseBody
    public String get() {
        for (HealthIndicator healthIndicator : healthIndicators) {
            Health health = healthIndicator.health();
            if (health.getStatus() != null && health.getStatus().equals(Status.UP)) {
                continue;
            } else {
                return health.toString();
            }
        }

        return "ok";
    }

}
