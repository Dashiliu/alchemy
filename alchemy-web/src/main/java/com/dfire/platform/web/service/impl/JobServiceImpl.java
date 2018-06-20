package com.dfire.platform.web.service.impl;

import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dfire.platform.web.common.Status;
import com.dfire.platform.web.common.Valid;
import com.dfire.platform.web.configuration.Flame;
import com.dfire.platform.web.data.AcJob;
import com.dfire.platform.web.data.AcJobRepository;
import com.dfire.platform.web.service.JobService;
import com.dfire.platform.web.service.dto.JobDTO;
import com.dfire.platform.web.web.vm.JobVM;
import com.google.common.base.Preconditions;

/**
 * @author congbai
 * @date 2018/6/8
 */
@Service
@Transactional
public class JobServiceImpl implements JobService {

    private static final long DEFAUL_SERVICE_ID = -1;

    private final AcJobRepository jobRepository;

    private final Flame flame;

    public JobServiceImpl(AcJobRepository jobRepository, Flame flame) {
        this.jobRepository = jobRepository;
        this.flame = flame;
    }

    @Override
    public void save(JobVM jobVM) {
        AcJob acJob = createJob(jobVM);
        this.jobRepository.save(acJob);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<JobDTO> list(Pageable pageable) {
        AcJob query = new AcJob();
        query.setIsValid(Valid.VALID.getValid());
        Page<AcJob> acJobPage = this.jobRepository.findAll(Example.of(query), pageable);
        return acJobPage.map(acJob -> {
            JobDTO job = new JobDTO();
            BeanUtils.copyProperties(acJob, job);
            return job;
        });
    }

    @Override
    public void updateStatus(Long id, int status) {
        AcJob acJob = this.jobRepository.findOne(id);
        Preconditions.checkNotNull(acJob, "job don't exist");
        acJob.setStatus(status);
        this.jobRepository.saveAndFlush(acJob);
    }

    @Override
    public void delete(Long id) {
        AcJob acJob = this.jobRepository.findOne(id);
        Preconditions.checkNotNull(acJob, "job don't exist");
        acJob.setIsValid(Valid.DEL.getValid());
        this.jobRepository.saveAndFlush(acJob);
    }

    private AcJob createJob(JobVM jobVM) {
        Preconditions.checkNotNull(jobVM.getName(), "job name can't be null");
        Preconditions.checkNotNull(jobVM.getSubmitMode(), "job submitMode can't be null");
        AcJob acJob = new AcJob();
        acJob.setId(flame.nextId());
        acJob.setAcServiceId(jobVM.getAcServiceId() == null ? DEFAUL_SERVICE_ID : jobVM.getAcServiceId());
        acJob.setCreateTime(new Date());
        acJob.setName(jobVM.getName());
        acJob.setStatus(Status.UN_FIX.getStatus());
        acJob.setSubmitMode(jobVM.getSubmitMode());
        return acJob;
    }

}
