package com.dfire.platform.alchemy.web.rest;

import com.dfire.platform.alchemy.web.rest.util.PaginationUtil;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/check_health")
public class HealthResource {

    private final HealthIndicator[] healthIndicators;


    public HealthResource(HealthIndicator...healthIndicators) {
        this.healthIndicators = healthIndicators;
    }


    @GetMapping
    @ResponseBody
    public String get() {
        for(HealthIndicator healthIndicator:healthIndicators){
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
