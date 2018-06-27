package com.dfire.platform.alchemy.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.codahale.metrics.annotation.Timed;
import com.dfire.platform.alchemy.web.domain.User;
import com.dfire.platform.alchemy.web.rest.util.HeaderUtil;
import com.dfire.platform.alchemy.web.rest.vm.JobConfVM;
import com.dfire.platform.alchemy.web.security.AuthoritiesConstants;
import com.dfire.platform.alchemy.web.service.JobConfService;
import com.dfire.platform.alchemy.web.service.dto.JobConfDTO;

/**
 * @author congbai
 * @date 2018/6/8
 */
@RestController
@RequestMapping("/api")
public class JobConfController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobConfController.class);

    private final JobConfService jobConfService;

    public JobConfController(JobConfService jobConfService) {
        this.jobConfService = jobConfService;
    }

    @PostMapping("/confs")
    @Timed
    public ResponseEntity<User> createJobConf(@Valid @RequestBody JobConfVM confVM) throws URISyntaxException {
        LOGGER.debug("REST request to save jobConf : {}", confVM);
        jobConfService.save(confVM);
        return ResponseEntity.created(new URI("/api/confs"))
            .headers(HeaderUtil.createAlert("A jobConf is created ", null)).build();

    }

    @PutMapping("/confs")
    @Timed
    public ResponseEntity<JobConfDTO> updateJobConf(@Valid @RequestBody JobConfVM jobConfVM) throws URISyntaxException {
        LOGGER.debug("REST request to update Jobconf : {}", jobConfVM);
        jobConfService.update(jobConfVM);

        return ResponseEntity.created(new URI("/api/confs"))
            .headers(HeaderUtil.createAlert("A jobConf is updated ", null)).build();
    }

    @GetMapping(value = "/confs", params = {"jobId", "type"})
    @Timed
    public ResponseEntity<List<JobConfDTO>> getJobConf(@RequestParam(value = "jobId") Long jobId,
        @RequestParam(value = "type") int type) {
        LOGGER.debug("REST request to get JobConf ,jobid: {},type:{}", jobId, type);
        final List<JobConfDTO> jobDTOList = jobConfService.findByType(jobId, type);
        return new ResponseEntity<>(jobDTOList, HeaderUtil.createAlert("get jobConf ", null), HttpStatus.OK);
    }

    @DeleteMapping("/confs/{id}")
    @Timed
    public ResponseEntity<Void> deleteJobConf(@PathVariable Long id) {
        LOGGER.debug("REST request to delete job : {}", id);
        jobConfService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("A jobConf is deleted ", null)).build();
    }
}
