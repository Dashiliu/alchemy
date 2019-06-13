package com.dfire.platform.alchemy.service;

import com.dfire.platform.alchemy.client.ClientManager;
import com.dfire.platform.alchemy.client.FlinkClient;
import com.dfire.platform.alchemy.client.request.JobStatusRequest;
import com.dfire.platform.alchemy.client.response.JobStatusResponse;
import com.dfire.platform.alchemy.domain.Business;
import com.dfire.platform.alchemy.domain.Job;
import com.dfire.platform.alchemy.domain.enumeration.JobStatus;
import com.dfire.platform.alchemy.repository.BusinessRepository;
import com.dfire.platform.alchemy.repository.JobRepository;
import com.dfire.platform.alchemy.repository.UserRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.hadoop.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class StatuService implements InitializingBean {

    private final Logger log = LoggerFactory.getLogger(StatuService.class);

    private final JobRepository jobRepository;

    private final BusinessRepository businessRepository;

    private final UserRepository userRepository;

    private final ClientManager clientManager;

    private final DingTalkService dingTalkService;

    private final ScheduledExecutorService executorService ;

    public StatuService(JobRepository jobRepository, BusinessRepository businessRepository, UserRepository userRepository, ClientManager clientManager, DingTalkService dingTalkService) {
        this.jobRepository = jobRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.clientManager = clientManager;
        this.dingTalkService = dingTalkService;
        this.executorService = Executors.newScheduledThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("job-status-%s").build());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executorService.scheduleAtFixedRate(new Executor(), 10, 10, TimeUnit.SECONDS);
    }

    public class Executor implements Runnable{

        @Override
        public void run() {
            List<Business> businesses = businessRepository.findAll();
            if(CollectionUtils.isEmpty(businesses)){
                return;
            }
            try {
                for(Business business : businesses){
                    List<Job> jobs = jobRepository.findAllByBusinessId(business.getId());
                    if(CollectionUtils.isEmpty(jobs)){
                        continue;
                    }
                    for(Job job : jobs){
                        if(JobStatus.SUBMIT != job.getStatus() && JobStatus.RUNNING != job.getStatus()){
                            continue;
                        }
                        FlinkClient client =clientManager.getClient(job.getCluster().getId());
                        JobStatusResponse jobStatusResponse = client.status(new JobStatusRequest(job.getClusterJobId()));
                        if(jobStatusResponse.isSuccess()){
                            JobStatus jobStatus =jobStatusResponse.getStatus();
                            if(jobStatus != job.getStatus()){
                                job.setStatus(jobStatus);
                                jobRepository.save(job);
                                if(JobStatus.FAILED == jobStatus){
                                    dingTalkService.sendMessage("####任务失败", String.format("####业务：%s \n ##### 任务：%s", business.getName(), job.getName()));
                                }
                            }
                        }else{
                            log.warn("request status failed, msg:{}", jobStatusResponse.getMessage());
                        }
                    }
                }
            }catch (Exception e){

            }
        }
    }
}
