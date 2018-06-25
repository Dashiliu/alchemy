package com.dfire.platform.alchemy.web.rest;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.alchemy.web.repository.AcServiceRepository;
import com.dfire.platform.alchemy.web.rest.vm.JobVM;
import com.dfire.platform.alchemy.web.service.JobService;
import com.dfire.platform.alchemy.web.service.dto.JobDTO;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import com.twodfire.share.util.ResultUtil;

import io.swagger.annotations.ApiParam;

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

    public JobController(AcServiceRepository acServiceRepository, JobService jobService) {
        this.acServiceRepository = acServiceRepository;
        this.jobService = jobService;
    }

    @PostMapping("/job")
    @ResponseBody
    public Result create(@Valid @RequestBody JobVM jobVM) {
        LOGGER.debug("REST request to save job : {}", jobVM);
        jobService.save(jobVM);
        return ResultUtil.defaultResult();
    }

    @GetMapping("/job")
    @ResponseBody
    public Result<JobDTO> getAllJobs(@ApiParam Pageable pageable) {
        final Page<JobDTO> page = jobService.list(pageable);
        ResultSupport resultSupport = new ResultSupport();
        resultSupport.setModel(page.getContent());
        resultSupport.setTotalRecord(Integer.parseInt(String.valueOf(page.getTotalElements())));
        return resultSupport;
    }

    @DeleteMapping("/job/{id}")
    @ResponseBody
    public Result deleteService(@PathVariable Long id) {
        LOGGER.debug("REST request to delete job : {}", id);
        jobService.delete(id);
        return ResultUtil.defaultResult();
    }

}
