package com.dfire.platform.alchemy.web.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dfire.platform.alchemy.web.common.Status;
import com.dfire.platform.alchemy.web.config.Flame;
import com.dfire.platform.alchemy.web.domain.AcJobFailMsg;
import com.dfire.platform.alchemy.web.repository.AcJobFailMsgRepository;
import com.dfire.platform.alchemy.web.rest.vm.JobFailVM;
import com.dfire.platform.alchemy.web.service.JobFailService;
import com.dfire.platform.alchemy.web.service.JobService;
import com.dfire.platform.alchemy.web.service.dto.JobFailMsgDTO;

/**
 * @author congbai
 * @date 2018/6/19
 */
@Service
@Transactional
public class JobFailServiceImpl implements JobFailService {

    private final AcJobFailMsgRepository jobFailMsgRepository;

    private final JobService jobService;

    private final Flame flame;

    public JobFailServiceImpl(AcJobFailMsgRepository jobFailMsgRepository, JobService jobService, Flame flame) {
        this.jobFailMsgRepository = jobFailMsgRepository;
        this.jobService = jobService;
        this.flame = flame;
    }

    @Override
    public void save(JobFailVM jobFailVM) {
        this.jobService.updateStatus(jobFailVM.getAcJobId(), Status.AUDIT_FAIL.getStatus());
        AcJobFailMsg jobFailMsg = new AcJobFailMsg();
        jobFailMsg.setId(flame.nextId());
        jobFailMsg.setAcJobId(jobFailMsg.getAcJobId());
        jobFailMsg.setMsg(jobFailMsg.getMsg());
        this.jobFailMsgRepository.save(jobFailMsg);
    }

    @Transactional(readOnly = true)
    @Override
    public JobFailMsgDTO find(Long acJobId) {
        AcJobFailMsg query = new AcJobFailMsg();
        query.setAcJobId(acJobId);
        Page<AcJobFailMsg> jobFailMsgs = this.jobFailMsgRepository.findAll(Example.of(query),
            new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "create_time")));
        if (jobFailMsgs.getTotalElements() <= 0) {
            return null;
        }
        JobFailMsgDTO jobFailMsgDTO = new JobFailMsgDTO();
        BeanUtils.copyProperties(jobFailMsgs.getContent().get(0), jobFailMsgDTO);
        return jobFailMsgDTO;
    }
}
