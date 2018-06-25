package com.dfire.platform.alchemy.web.rest;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.alchemy.web.common.Status;
import com.dfire.platform.alchemy.web.rest.vm.JobFailVM;
import com.dfire.platform.alchemy.web.service.ClusterJobService;
import com.dfire.platform.alchemy.web.service.JobFailService;
import com.dfire.platform.alchemy.web.service.JobService;
import com.twodfire.share.result.Result;
import com.twodfire.share.util.ResultUtil;

/**
 * @author congbai
 * @date 2018/6/8
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final Logger log = LoggerFactory.getLogger(AuditController.class);

    private final ClusterJobService submitService;

    private final JobService jobService;

    private final JobFailService jobFailService;

    public AuditController(ClusterJobService submitService, JobService jobService, JobFailService jobFailService) {
        this.submitService = submitService;
        this.jobService = jobService;
        this.jobFailService = jobFailService;
    }

    @GetMapping(value = "/pass", params = {"acJobId"})
    @ResponseBody
    public Result pass(@RequestParam(value = "acJobId") @NotEmpty Long acJobId) {
        this.jobService.updateStatus(acJobId, Status.AUDIT_PASS.getStatus());
        this.submitService.submit(acJobId);
        return ResultUtil.defaultResult();
    }

    @PostMapping(value = "/fail")
    @ResponseBody
    public Result fail(@Valid @RequestBody JobFailVM auditVM) {
        this.jobFailService.save(auditVM);
        return ResultUtil.defaultResult();
    }

}
