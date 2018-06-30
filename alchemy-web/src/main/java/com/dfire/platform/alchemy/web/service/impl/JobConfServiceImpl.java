package com.dfire.platform.alchemy.web.service.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.common.*;
import com.dfire.platform.alchemy.web.config.Flame;
import com.dfire.platform.alchemy.web.descriptor.DescriptorManager;
import com.dfire.platform.alchemy.web.descriptor.TableDescriptor;
import com.dfire.platform.alchemy.web.domain.AcJob;
import com.dfire.platform.alchemy.web.domain.AcJobConf;
import com.dfire.platform.alchemy.web.repository.AcJobConfRepository;
import com.dfire.platform.alchemy.web.repository.AcJobRepository;
import com.dfire.platform.alchemy.web.rest.vm.JobConfVM;
import com.dfire.platform.alchemy.web.service.JobConfService;
import com.dfire.platform.alchemy.web.service.JobService;
import com.dfire.platform.alchemy.web.service.dto.JobConfDTO;
import com.dfire.platform.alchemy.web.util.JsonUtils;
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
        acJobConf.setContent(JsonUtils.toJson(jobConfVM.getContent()));
        acJobConf.setCreateTime(new Date());
        acJobConf.setIsValid(Valid.VALID.getValid());
        return acJobConf;
    }

    private void checkArguments(JobConfVM jobConfVM) {
        Preconditions.checkNotNull(jobConfVM.getAcJobId(), "jobId can't be null");
        Preconditions.checkNotNull(jobConfVM.getType(), "type can't be null");
        Preconditions.checkNotNull(jobConfVM.getContent(), "content can't be null");
        Content content = jobConfVM.getContent();
        if (ConfType.CONFIG.getType() == jobConfVM.getType()) {
            try {
                TableDescriptor tableDescriptor = new TableDescriptor();
                BindPropertiesFactory.bindPropertiesToTarget(tableDescriptor, Constants.BIND_PREFIX_TABLE,
                    content.getConfig());
                tableDescriptor.validate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void update(JobConfVM jobConfVM) {
        checkArguments(jobConfVM);
        Optional<AcJobConf> acJobConfOptional = this.jobConfRepository.findById(jobConfVM.getId());
        Preconditions.checkArgument(!acJobConfOptional.isPresent(), "jobConf is null");
        Optional<AcJob> acJob = this.jobRepository.findById(jobConfVM.getAcJobId());
        Preconditions.checkNotNull(acJob, "job is null");
        AcJobConf acJobConf = createJobConf(jobConfVM);
        this.jobConfRepository.save(acJobConf);
        acJob.get().setStatus(Status.UN_AUDIT.getStatus());
        this.jobRepository.save(acJob.get());
        this.jobConfRepository.flush();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JobConfDTO> findByType(Long jobId, int type) {
        LOGGER.trace("start find JobConf,jobId:{},type:{}", jobId, type);
        AcJobConf query = new AcJobConf();
        query.setIsValid(Valid.VALID.getValid());
        query.setAcJobId(jobId);
        query.setType(type);
        List<AcJobConf> acJobConfs
            = this.jobConfRepository.findAll(Example.of(query), new Sort(Sort.Direction.DESC, "updateTime"));
        if (CollectionUtils.isEmpty(acJobConfs)) {
            LOGGER.trace("JobConf is empty,type:{}", type);
            return Collections.emptyList();
        }
        List<JobConfDTO> jobConfDTOs = new ArrayList<>(acJobConfs.size());
        for (AcJobConf acJobConf : acJobConfs) {
            JobConfDTO jobConfDTO = createJobConfDTO(acJobConf);
            jobConfDTOs.add(jobConfDTO);
        }
        return jobConfDTOs;
    }

    @Override
    public List<JobConfDTO> findByJobId(Long jobId) {
        LOGGER.trace("start find JobConf,jobId:{}", jobId);
        AcJobConf query = new AcJobConf();
        query.setIsValid(Valid.VALID.getValid());
        query.setAcJobId(jobId);
        List<AcJobConf> acJobConfs
            = this.jobConfRepository.findAll(Example.of(query), new Sort(Sort.Direction.DESC, "updateTime"));
        if (CollectionUtils.isEmpty(acJobConfs)) {
            LOGGER.trace("JobConf is empty,jobId:{}", jobId);
            return Collections.emptyList();
        }
        List<JobConfDTO> jobConfDTOs = new ArrayList<>(acJobConfs.size());
        for (AcJobConf acJobConf : acJobConfs) {
            JobConfDTO jobConfDTO = createJobConfDTO(acJobConf);
            jobConfDTOs.add(jobConfDTO);
        }
        return jobConfDTOs;
    }

    private JobConfDTO createJobConfDTO(AcJobConf acJobConf) {
        JobConfDTO jobConfDTO = new JobConfDTO();
        BeanUtils.copyProperties(acJobConf, jobConfDTO);
        jobConfDTO.setContent(JsonUtils.fromJson(acJobConf.getContent(), Content.class));
        return jobConfDTO;
    }

    @Transactional(readOnly = true)
    @Override
    public List<JobConfDTO> findAll() {
        LOGGER.trace("start find  all JobConf");
        List<AcJobConf> acJobConfs
            = this.jobConfRepository.findByIsValid(Valid.VALID.getValid(), new Sort(Sort.Direction.DESC, "updateTime"));
        if (CollectionUtils.isEmpty(acJobConfs)) {
            LOGGER.trace("JobConf is empty");
            return Collections.emptyList();
        }
        List<JobConfDTO> jobConfDTOs = new ArrayList<>(acJobConfs.size());
        for (AcJobConf acJobConf : acJobConfs) {
            JobConfDTO jobConfDTO = createJobConfDTO(acJobConf);
            jobConfDTOs.add(jobConfDTO);
        }
        return jobConfDTOs;
    }

    @Override
    public void delete(Long id) {
        Optional<AcJobConf> acJobConf = this.jobConfRepository.findById(id);
        Preconditions.checkNotNull(acJobConf.get(), "jobConf don't exist");
        acJobConf.get().setIsValid(Valid.DEL.getValid());
        this.jobConfRepository.saveAndFlush(acJobConf.get());
    }
}
