package com.dfire.platform.web.web;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.dfire.platform.web.common.Status;
import com.dfire.platform.web.data.AcJobFailMsgRepository;
import com.dfire.platform.web.service.ClusterJobService;
import com.dfire.platform.web.service.JobService;
import com.dfire.platform.web.web.vm.JobFailVM;
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

    private final AcJobFailMsgRepository jobFailMsgRepository;

    public AuditController(ClusterJobService submitService, JobService jobService,
        AcJobFailMsgRepository jobFailMsgRepository) {
        this.submitService = submitService;
        this.jobService = jobService;
        this.jobFailMsgRepository = jobFailMsgRepository;
    }

    @GetMapping(value = "/pass", params = {"acJobId"})
    public Result pass(@RequestParam(value = "acJobId") @NotEmpty Long acJobId) {
        this.jobService.updateStatus(acJobId, Status.AUDIT_PASS.getStatus());
        this.submitService.submit(acJobId);
        return ResultUtil.defaultResult();
    }

    @PostMapping(value = "/fail")
    public Result fail(@Valid @RequestBody JobFailVM auditVM) {

        return ResultUtil.defaultResult();
    }

}
