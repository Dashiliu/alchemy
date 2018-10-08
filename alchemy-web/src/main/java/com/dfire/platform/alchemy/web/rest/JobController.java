package com.dfire.platform.alchemy.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import com.dfire.platform.alchemy.web.cluster.ClusterInfo;
import com.dfire.platform.alchemy.web.cluster.ClusterProperties;
import com.dfire.platform.alchemy.web.common.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.alchemy.web.repository.AcServiceRepository;
import com.dfire.platform.alchemy.web.rest.util.HeaderUtil;
import com.dfire.platform.alchemy.web.rest.util.PaginationUtil;
import com.dfire.platform.alchemy.web.rest.vm.JobVM;
import com.dfire.platform.alchemy.web.service.JobService;
import com.dfire.platform.alchemy.web.service.dto.JobDTO;

/**
 * @author congbai
 * @date 2018/6/8
 */
@RestController
@RequestMapping("/api")
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    private final AcServiceRepository acServiceRepository;

    private final JobService jobService;

    private final ClusterProperties clusterProperties;

    public JobController(AcServiceRepository acServiceRepository, JobService jobService, ClusterProperties clusterProperties) {
        this.acServiceRepository = acServiceRepository;
        this.jobService = jobService;
        this.clusterProperties = clusterProperties;
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobVM jobVM) throws URISyntaxException {
        LOGGER.debug("REST request to save job : {}", jobVM);

        jobService.save(jobVM);
        return ResponseEntity.created(new URI("/api/jobs/")).headers(HeaderUtil.createAlert("A job is created ", null))
            .build();
    }

    @GetMapping(value = "/jobs/status", params = {"jobId", "status"})
    public ResponseEntity<JobDTO> updateStatus(@RequestParam(value = "jobId") Long jobId,
        @RequestParam(value = "status") int status) throws URISyntaxException {
        LOGGER.debug("REST request to udpate job status,id : {},status: {}", jobId, status);
        jobService.updateStatus(jobId, status);
        return ResponseEntity.created(new URI("/api/jobs/"))
            .headers(HeaderUtil.createAlert("A job status is update ", null)).build();
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobDTO>> getAllJobs(Pageable pageable) {
        final Page<JobDTO> page = jobService.list(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/jobs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/jobs/clusters")
    public ResponseEntity<List<ClusterInfo>> clusters() {
        return new ResponseEntity<>(clusterProperties.getClusters(), null, HttpStatus.OK);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobDTO> getJob(@PathVariable Long id) {
        final JobDTO jobDTO = jobService.findById(id);
        return new ResponseEntity<>(jobDTO, HeaderUtil.createAlert("A job is deleted  ", null), HttpStatus.OK);
    }

    @GetMapping("/jobs/cancel/{id}")
    public ResponseEntity<JobDTO> cancel(@PathVariable Long id) throws URISyntaxException {
        jobService.updateStatus(id, Status.CANCELED.getStatus());
        return ResponseEntity.created(new URI("/api/jobs/"))
            .headers(HeaderUtil.createAlert("A job status is cancel ", null)).build();
    }

    @GetMapping("/jobs/restart/{id}")
    public ResponseEntity<JobDTO> restart(@PathVariable Long id) throws URISyntaxException {
        jobService.restart(id);
        return ResponseEntity.created(new URI("/api/jobs/"))
            .headers(HeaderUtil.createAlert("A job status is restart ", null)).build();
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        LOGGER.debug("REST request to delete Job: {}", id);
        jobService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("A job is deleted  ", null)).build();
    }

}
