package com.dfire.platform.alchemy.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.alchemy.web.rest.vm.JobConfVM;
import com.dfire.platform.alchemy.web.service.JobConfService;
import com.dfire.platform.alchemy.web.service.dto.JobConfDTO;
import com.dfire.platform.alchemy.web.service.dto.JobDTO;
import com.twodfire.share.result.Result;
import com.twodfire.share.util.ResultUtil;

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

    @PostMapping("/conf")
    @ResponseBody
    public Result create(@Valid @RequestBody JobConfVM confVM) {
        LOGGER.debug("REST request to save job conf: {}", confVM);
        jobConfService.save(confVM);
        return ResultUtil.defaultResult();
    }

    @GetMapping("/conf")
    @ResponseBody
    public Result<JobDTO> getAllJobs() {
        final List<JobConfDTO> jobDTOList = jobConfService.findAll();
        return ResultUtil.successResult(jobDTOList);
    }

    @PutMapping("/conf")
    @ResponseBody
    public Result updateUser(@Valid @RequestBody JobConfVM confVM) {
        LOGGER.debug("REST request to update Jobconf : {}", confVM);
        jobConfService.update(confVM);
        return ResultUtil.defaultResult();
    }

    @DeleteMapping("/conf/{id}")
    @ResponseBody
    public Result deleteService(@PathVariable Long id) {
        LOGGER.debug("REST request to delete job : {}", id);
        jobConfService.delete(id);
        return ResultUtil.defaultResult();
    }

}
