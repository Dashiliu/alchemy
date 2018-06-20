package com.dfire.platform.web.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.web.common.DescriptorData;
import com.dfire.platform.web.common.Status;
import com.dfire.platform.web.common.Valid;
import com.dfire.platform.web.configuration.Flame;
import com.dfire.platform.web.data.AcJob;
import com.dfire.platform.web.data.AcJobConf;
import com.dfire.platform.web.data.AcJobConfRepository;
import com.dfire.platform.web.data.AcJobRepository;
import com.dfire.platform.web.descriptor.Descriptor;
import com.dfire.platform.web.descriptor.DescriptorManager;
import com.dfire.platform.web.service.JobConfService;
import com.dfire.platform.web.service.JobService;
import com.dfire.platform.web.service.dto.JobConfDTO;
import com.dfire.platform.web.util.JsonUtils;
import com.dfire.platform.web.web.vm.JobConfVM;
import com.google.common.base.Preconditions;

/**
 * @author congbai
 * @date 2018/6/8
 */
@Service
@Transactional
public class JobConfServiceImpl implements JobConfService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobConfServiceImpl.class);

    private final Flame flame;

    private final AcJobConfRepository jobConfRepository;

    private final AcJobRepository jobRepository;

    private final JobService jobService;

    private final DescriptorManager descriptorManager;

    public JobConfServiceImpl(Flame flame, AcJobConfRepository jobConfRepository, AcJobRepository jobRepository,
        JobService jobService, DescriptorManager descriptorManager) {
        this.flame = flame;
        this.jobConfRepository = jobConfRepository;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.descriptorManager = descriptorManager;
    }

    @Override
    public void save(JobConfVM jobConfVM) {
        checkArguments(jobConfVM);
        AcJobConf acJobConf = createJobConf(jobConfVM);
        if (jobConfVM.isAudit()) {
            this.jobService.updateStatus(jobConfVM.getAcJobId(), Status.UN_AUDIT.getStatus());
        }
        this.jobConfRepository.save(acJobConf);
    }

    private AcJobConf createJobConf(JobConfVM jobConfVM) {
        AcJobConf acJobConf = new AcJobConf();
        acJobConf.setId(jobConfVM.getId() == null ? flame.nextId() : jobConfVM.getId());
        acJobConf.setType(jobConfVM.getType());
        acJobConf.setAcJobId(jobConfVM.getAcJobId());
        acJobConf.setContent(jobConfVM.getContent());
        acJobConf.setCreateTime(new Date());
        return acJobConf;
    }

    private void checkArguments(JobConfVM jobConfVM) {
        Preconditions.checkNotNull(jobConfVM.getAcJobId(), "jobId can't be null");
        Preconditions.checkNotNull(jobConfVM.getType(), "type can't be null");
        Preconditions.checkNotNull(jobConfVM.getContent(), "content can't be null");
        DescriptorData descriptorData = JsonUtils.fromJson(jobConfVM.getContent(), DescriptorData.class);
        Descriptor prototype = descriptorManager.getByContentType(descriptorData.getContentType());
        Descriptor descriptor = JsonUtils.fromJson(descriptorData.getDescriptor(), prototype.getClass());
        try {
            descriptor.validate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(JobConfVM jobConfVM) {
        checkArguments(jobConfVM);
        AcJobConf acJobConf = this.jobConfRepository.findOne(jobConfVM.getId());
        Preconditions.checkNotNull(acJobConf, "jobConf is null");
        AcJob acJob = this.jobRepository.findOne(jobConfVM.getAcJobId());
        Preconditions.checkNotNull(acJob, "job is null");
        acJobConf = createJobConf(jobConfVM);
        this.jobConfRepository.save(acJobConf);
        acJob.setStatus(Status.UN_AUDIT.getStatus());
        this.jobRepository.save(acJob);
        this.jobConfRepository.flush();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JobConfDTO> findByType(int type) {
        LOGGER.trace("start find JobConf,type:{}", type);
        AcJobConf query = new AcJobConf();
        query.setIsValid(Valid.VALID.getValid());
        query.setType(type);
        List<AcJobConf> acJobConfs
            = this.jobConfRepository.findAll(Example.of(query), new Sort(Sort.Direction.DESC, "update_time"));
        if (CollectionUtils.isEmpty(acJobConfs)) {
            LOGGER.trace("JobConf is empty,type:{}", type);
            return Collections.emptyList();
        }
        List<JobConfDTO> jobConfDTOs = new ArrayList<>(acJobConfs.size());
        for (AcJobConf acJobConf : acJobConfs) {
            JobConfDTO jobConfDTO = new JobConfDTO();
            BeanUtils.copyProperties(acJobConf, jobConfDTO);
            jobConfDTOs.add(jobConfDTO);
        }
        return jobConfDTOs;
    }

    @Override
    public void delete(Long id) {
        AcJobConf acJobConf = this.jobConfRepository.findOne(id);
        Preconditions.checkNotNull(acJobConf, "jobConf don't exist");
        acJobConf.setIsValid(Valid.DEL.getValid());
        this.jobConfRepository.saveAndFlush(acJobConf);
    }
}
