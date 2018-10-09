package com.dfire.platform.alchemy.web.service.impl;

import java.util.Date;
import java.util.Optional;

import com.dfire.platform.alchemy.web.service.ClusterJobService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dfire.magiceye.util.StringUtils;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.Status;
import com.dfire.platform.alchemy.web.common.Valid;
import com.dfire.platform.alchemy.web.config.Flame;
import com.dfire.platform.alchemy.web.domain.AcJob;
import com.dfire.platform.alchemy.web.repository.AcJobRepository;
import com.dfire.platform.alchemy.web.rest.vm.JobVM;
import com.dfire.platform.alchemy.web.service.JobService;
import com.dfire.platform.alchemy.web.service.dto.JobDTO;
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

    private final ClusterJobService submitService;

    private final Flame flame;

    public JobServiceImpl(AcJobRepository jobRepository, ClusterJobService submitService, Flame flame) {
        this.jobRepository = jobRepository;
        this.submitService = submitService;
        this.flame = flame;
    }

    @Override
    public void restart(Long id) {
        updateStatus(id,Status.AUDIT_PASS.getStatus());
        this.submitService.submit(id);
    }

    @Override
    public void save(JobVM jobVM) {
        AcJob acJob = createJob(jobVM);
        this.jobRepository.save(acJob);
    }

    @Override
    public void updateCluster(Long id, String cluster) {
        Preconditions.checkNotNull(cluster, "cluster can't be null");
        Optional<AcJob> acJob = this.jobRepository.findById(id);
        Preconditions.checkNotNull(acJob.get(), "job don't exist");
        acJob.get().setStatus(Status.AUDIT_PASS.getStatus());
        acJob.get().setCluster(cluster);
        this.jobRepository.saveAndFlush(acJob.get());
        this.submitService.submit(id);
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
        Status statusEnum=Status.fromStatus(status);
        switch (statusEnum){
            case AUDIT_PASS:
                this.submitService.submit(id);
                break;
            case CANCELED:
                this.submitService.cancel(id);
                break;
            default:
        }
        Optional<AcJob> acJob = this.jobRepository.findById(id);
        Preconditions.checkNotNull(acJob.get(), "job don't exist");
        acJob.get().setStatus(status);
        this.jobRepository.saveAndFlush(acJob.get());
    }

    @Override
    public void delete(Long id) {
        this.submitService.cancel(id);
        Optional<AcJob> acJob = this.jobRepository.findById(id);
        Preconditions.checkNotNull(acJob.get(), "job don't exist");
        acJob.get().setIsValid(Valid.DEL.getValid());
        this.jobRepository.saveAndFlush(acJob.get());
    }

    @Override
    @Transactional(readOnly = true)
    public JobDTO findById(Long id) {
        Optional<AcJob> acJob = this.jobRepository.findById(id);
        if (acJob.isPresent()) {
            JobDTO job = new JobDTO();
            BeanUtils.copyProperties(acJob.get(), job);
            return job;
        } else {
            return null;
        }
    }

    private AcJob createJob(JobVM jobVM) {
        Preconditions.checkNotNull(jobVM.getName(), "job name can't be null");
        Preconditions.checkNotNull(jobVM.getSubmitMode(), "job submitMode can't be null");
        AcJob acJob = new AcJob();
        acJob.setId(flame.nextId());
        acJob.setAcServiceId(jobVM.getAcServiceId() == null ? DEFAUL_SERVICE_ID : jobVM.getAcServiceId());
        acJob.setCreateTime(new Date());
        acJob.setName(jobVM.getName());
        if (StringUtils.isEmpty(jobVM.getCluster())) {
            acJob.setCluster(Constants.DEFAULT_FLINK_CLUSTER);
        } else {
            acJob.setCluster(jobVM.getCluster());
        }
        acJob.setStatus(Status.UN_FIX.getStatus());
        acJob.setSubmitMode(jobVM.getSubmitMode());
        acJob.setIsValid(Valid.VALID.getValid());
        return acJob;
    }

}
