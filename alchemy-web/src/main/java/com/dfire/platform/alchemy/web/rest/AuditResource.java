package com.dfire.platform.alchemy.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.alchemy.web.common.Status;
import com.dfire.platform.alchemy.web.rest.util.HeaderUtil;
import com.dfire.platform.alchemy.web.rest.util.PaginationUtil;
import com.dfire.platform.alchemy.web.rest.vm.JobFailVM;
import com.dfire.platform.alchemy.web.service.AuditEventService;
import com.dfire.platform.alchemy.web.service.ClusterJobService;
import com.dfire.platform.alchemy.web.service.JobFailService;
import com.dfire.platform.alchemy.web.service.JobService;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for getting the audit events.
 */
@RestController
@RequestMapping("/management/audits")
public class AuditResource {

    private final AuditEventService auditEventService;

    private final JobService jobService;

    private final JobFailService jobFailService;

    public AuditResource(AuditEventService auditEventService,
                         JobService jobService,
                         JobFailService jobFailService) {
        this.auditEventService = auditEventService;
        this.jobService = jobService;
        this.jobFailService = jobFailService;
    }

    /**
     * GET /audits : get a page of AuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAll(Pageable pageable) {
        Page<AuditEvent> page = auditEventService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /audits : get a page of AuditEvents between the fromDate and toDate.
     *
     * @param fromDate the start of the time period of AuditEvents to get
     * @param toDate the end of the time period of AuditEvents to get
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping(params = {"fromDate", "toDate"})
    public ResponseEntity<List<AuditEvent>> getByDates(@RequestParam(value = "fromDate") LocalDate fromDate,
        @RequestParam(value = "toDate") LocalDate toDate, Pageable pageable) {

        Page<AuditEvent> page = auditEventService.findByDates(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant(), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /audits/:id : get an AuditEvent by id.
     *
     * @param id the id of the entity to get
     * @return the ResponseEntity with status 200 (OK) and the AuditEvent in body, or status 404 (Not Found)
     */
    @GetMapping("/{id:.+}")
    public ResponseEntity<AuditEvent> get(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(auditEventService.find(id));
    }

    @GetMapping(value = "/pass", params = {"jobId"})
    public ResponseEntity<Void> pass(@RequestParam(value = "jobId") @NotEmpty Long acJobId,
                                     @RequestParam(value = "cluster") @NotEmpty String cluster) throws URISyntaxException {
        this.jobService.updateCluster(acJobId, cluster);
        return ResponseEntity.created(new URI("/management/audits/pass"))
            .headers(HeaderUtil.createAlert("A job is passed ", null)).build();
    }

    @PostMapping(value = "/fail")
    public ResponseEntity<Void> fail(@Valid @RequestBody JobFailVM auditVM) throws URISyntaxException {
        this.jobFailService.save(auditVM);
        return ResponseEntity.created(new URI("/management/audits/fail"))
            .headers(HeaderUtil.createAlert("A job is failed ", null)).build();
    }
}
