package com.dfire.platform.web.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dfire.platform.web.configuration.Flame;
import com.dfire.platform.web.data.AcJobFailMsg;
import com.dfire.platform.web.data.AcJobFailMsgRepository;
import com.dfire.platform.web.service.JobFailService;
import com.dfire.platform.web.service.dto.JobFailMsgDTO;
import com.dfire.platform.web.web.vm.JobFailVM;

/**
 * @author congbai
 * @date 2018/6/19
 */
@Service
@Transactional
public class JobFailServiceImpl implements JobFailService {

    private final AcJobFailMsgRepository jobFailMsgRepository;

    private final Flame flame;

    public JobFailServiceImpl(AcJobFailMsgRepository jobFailMsgRepository, Flame flame) {
        this.jobFailMsgRepository = jobFailMsgRepository;
        this.flame = flame;
    }

    @Override
    public void save(JobFailVM jobFailVM) {
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
