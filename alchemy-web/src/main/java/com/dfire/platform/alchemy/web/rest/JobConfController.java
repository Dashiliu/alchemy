package com.dfire.platform.alchemy.web.rest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.dfire.platform.alchemy.web.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.Content;
import com.dfire.platform.alchemy.web.common.JarInfo;
import com.dfire.platform.alchemy.web.domain.User;
import com.dfire.platform.alchemy.web.rest.util.HeaderUtil;
import com.dfire.platform.alchemy.web.rest.vm.JobConfVM;
import com.dfire.platform.alchemy.web.service.JobConfService;
import com.dfire.platform.alchemy.web.service.dto.JobConfDTO;
import com.dfire.platform.alchemy.web.util.FileUtils;

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
    public ResponseEntity<JobConfDTO> getJobConf(@RequestParam(value = "jobId") Long jobId,
        @RequestParam(value = "type") Integer type) {
        LOGGER.debug("REST request to get JobConf ,jobid: {}", jobId);
        final List<JobConfDTO> jobDTOList = jobConfService.findByType(jobId, type);
        JobConfDTO jobConfDTO;
        if(CollectionUtils.isEmpty(jobDTOList)){
            jobConfDTO=new JobConfDTO();
            jobConfDTO.setAcJobId(jobId);
            jobConfDTO.setType(type);
            jobConfDTO.setContent(new Content());
        }else{
            jobConfDTO=jobDTOList.get(0);
        }
        return new ResponseEntity<>(jobConfDTO,
            HeaderUtil.createAlert("get jobConf ", null), HttpStatus.OK);
    }

    @DeleteMapping("/confs/{id}")
    @Timed
    public ResponseEntity<Void> deleteJobConf(@PathVariable Long id) {
        LOGGER.debug("REST request to delete jobConf : {}", id);
        jobConfService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("A jobConf is deleted ", null)).build();
    }

}
